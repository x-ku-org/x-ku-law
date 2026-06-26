package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.client.MqProducer;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.system.convert.NotificationConvert;
import cn.xku.law.system.domain.NotificationDO;
import cn.xku.law.system.domain.NotificationReceiverDO;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.domain.UserRoleDO;
import cn.xku.law.system.domain.dto.NotificationCreateDTO;
import cn.xku.law.system.domain.dto.NotificationInboxQueryDTO;
import cn.xku.law.system.domain.dto.NotificationQueryDTO;
import cn.xku.law.system.domain.vo.NotificationInboxVO;
import cn.xku.law.system.domain.vo.NotificationVO;
import cn.xku.law.system.mapper.NotificationMapper;
import cn.xku.law.system.mapper.NotificationReceiverMapper;
import cn.xku.law.system.mapper.UserMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import cn.xku.law.system.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, NotificationDO>
        implements NotificationService {

    private final NotificationConvert convert;
    private final NotificationReceiverMapper receiverMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final MqProducer mqProducer;

    @Override
    public PageResult<NotificationVO> pageNotifications(NotificationQueryDTO query) {
        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<NotificationDO>()
                .eq(StringUtils.hasText(query.getNotificationType()), NotificationDO::getNotificationType, query.getNotificationType())
                .eq(StringUtils.hasText(query.getStatus()), NotificationDO::getStatus, query.getStatus())
                .orderByDesc(NotificationDO::getCreateTime);
        IPage<NotificationDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public PageResult<NotificationInboxVO> pageInbox(NotificationInboxQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<NotificationReceiverDO> wrapper = new LambdaQueryWrapper<NotificationReceiverDO>()
                .eq(NotificationReceiverDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getReadStatus()),
                        NotificationReceiverDO::getReadStatus, query.getReadStatus())
                .orderByDesc(NotificationReceiverDO::getId);
        IPage<NotificationReceiverDO> receiverPage = receiverMapper.selectPage(query.toPage(), wrapper);

        if (receiverPage.getRecords().isEmpty()) {
            return PageResult.of(receiverPage.getTotal(), List.of());
        }

        // 批量取通知主体
        List<Long> notificationIds = receiverPage.getRecords().stream()
                .map(NotificationReceiverDO::getNotificationId).collect(Collectors.toList());
        Map<Long, NotificationDO> notifMap = this.listByIds(notificationIds).stream()
                .collect(Collectors.toMap(NotificationDO::getId, n -> n));

        List<NotificationInboxVO> vos = receiverPage.getRecords().stream().map(r -> {
            NotificationInboxVO vo = new NotificationInboxVO();
            vo.setReceiverId(r.getId());
            vo.setNotificationId(r.getNotificationId());
            vo.setReadStatus(r.getReadStatus());
            vo.setReadTime(r.getReadTime());
            NotificationDO n = notifMap.get(r.getNotificationId());
            if (n != null) {
                vo.setNotificationType(n.getNotificationType());
                vo.setTitle(n.getTitle());
                vo.setContent(n.getContent());
                vo.setRefType(n.getRefType());
                vo.setRefId(n.getRefId());
                vo.setSendTime(n.getSendTime());
                vo.setCreateTime(n.getCreateTime());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(receiverPage.getTotal(), vos);
    }

    @Override
    @Transactional
    public Long createNotification(NotificationCreateDTO dto) {
        normalizeCreateDto(dto);
        NotificationDO notification = convert.toDO(dto);
        if (!StringUtils.hasText(notification.getNotificationType())) {
            notification.setNotificationType("system");
        }
        if (!StringUtils.hasText(notification.getSendScope())) {
            notification.setSendScope("all");
        }
        this.save(notification);

        String channel = StringUtils.hasText(dto.getChannel()) ? dto.getChannel() : "station";
        List<NotificationReceiverDO> receivers = buildReceivers(notification.getId(), dto, channel);

        if (!receivers.isEmpty()) {
            for (NotificationReceiverDO r : receivers) {
                receiverMapper.insert(r);
            }
            // TODO(二期): 真实 MQ/邮件/短信网关接入后由回调更新 send_status；当前状态保持 pending
        }

        return notification.getId();
    }

    @Override
    @Transactional
    public boolean createStationNotification(Long userId, String title, String content,
                                             String refType, Long refId) {
        // 订阅预警在 @Async（AFTER_COMMIT）中触发，无安全上下文，tenant 默认为 0；
        // 通知/接收表是租户隔离表，必须按接收用户的真实租户落库，否则其收件箱（按本租户过滤）查不到。
        UserDO user = userMapper.selectActiveByIdIgnoreTenant(userId);
        if (user == null) {
            log.warn("[Notification] 站内信投递跳过：用户不存在 userId={}", userId);
            return false;
        }
        LocalDateTime now = LocalDateTime.now();

        NotificationDO notification = new NotificationDO();
        notification.setNotificationType("alert");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRefType(refType);
        notification.setRefId(refId);
        notification.setSendScope("single");
        notification.setStatus("sent");
        notification.setSendTime(now);
        notification.setTenantId(user.getTenantId()); // 显式置租户，覆盖插件默认注入的 0
        this.save(notification);

        NotificationReceiverDO receiver = new NotificationReceiverDO();
        receiver.setNotificationId(notification.getId());
        receiver.setUserId(userId);
        receiver.setChannel("station");
        receiver.setReceiver(String.valueOf(userId));
        receiver.setReadStatus("unread");
        receiver.setSendStatus("sent");
        receiver.setTenantId(user.getTenantId());
        receiverMapper.insert(receiver);
        return true;
    }

    @Override
    public void removeNotification(Long id) {
        if (!this.removeById(id)) throw new AppException(ErrorCode.NOT_FOUND);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        LambdaQueryWrapper<NotificationReceiverDO> wrapper = new LambdaQueryWrapper<NotificationReceiverDO>()
                .eq(NotificationReceiverDO::getNotificationId, notificationId)
                .eq(NotificationReceiverDO::getUserId, userId)
                .eq(NotificationReceiverDO::getReadStatus, "unread");
        NotificationReceiverDO receiver = receiverMapper.selectOne(wrapper);
        if (receiver == null) return;
        receiver.setReadStatus("read");
        receiver.setReadTime(LocalDateTime.now());
        receiverMapper.updateById(receiver);
    }

    @Override
    public long markAllRead(Long userId) {
        long pending = countUnread(userId);
        if (pending == 0L) {
            return 0L;
        }
        NotificationReceiverDO patch = new NotificationReceiverDO();
        patch.setReadStatus("read");
        patch.setReadTime(LocalDateTime.now());
        receiverMapper.update(patch, new LambdaQueryWrapper<NotificationReceiverDO>()
                .eq(NotificationReceiverDO::getUserId, userId)
                .eq(NotificationReceiverDO::getReadStatus, "unread"));
        return pending;
    }

    @Override
    public long countUnread(Long userId) {
        return receiverMapper.selectCount(new LambdaQueryWrapper<NotificationReceiverDO>()
                .eq(NotificationReceiverDO::getUserId, userId)
                .eq(NotificationReceiverDO::getReadStatus, "unread"));
    }

    private void normalizeCreateDto(NotificationCreateDTO dto) {
        if ("user".equals(dto.getSendScope())) {
            dto.setSendScope("single");
        }
    }

    private List<NotificationReceiverDO> buildReceivers(Long notificationId,
                                                         NotificationCreateDTO dto, String channel) {
        String scope = dto.getSendScope();
        List<NotificationReceiverDO> result = new ArrayList<>();

        if ("single".equals(scope) && dto.getTargetUserId() != null) {
            UserDO user = userMapper.selectById(dto.getTargetUserId());
            if (user != null) {
                result.add(buildReceiver(notificationId, user, channel));
            }
        } else if ("role".equals(scope) && dto.getTargetRoleId() != null) {
            List<UserRoleDO> userRoles = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, dto.getTargetRoleId()));
            List<Long> userIds = userRoles.stream().map(UserRoleDO::getUserId).collect(Collectors.toList());
            if (!userIds.isEmpty()) {
                List<UserDO> users = userMapper.selectBatchIds(userIds);
                for (UserDO u : users) result.add(buildReceiver(notificationId, u, channel));
            }
        } else if ("tenant".equals(scope)) {
            List<UserDO> users = userMapper.selectList(
                    new LambdaQueryWrapper<UserDO>().eq(UserDO::getStatus, "enabled").last("LIMIT 500"));
            for (UserDO u : users) result.add(buildReceiver(notificationId, u, channel));
        } else if ("all".equals(scope)) {
            // TODO(二期): 全量用户需要分批异步处理，生产环境严禁同步全量创建
            log.warn("[Notification] sendScope=all 当前仅处理前500条，生产环境需改为异步分批");
            List<UserDO> users = userMapper.selectList(
                    new LambdaQueryWrapper<UserDO>().eq(UserDO::getStatus, "enabled").last("LIMIT 500"));
            for (UserDO u : users) result.add(buildReceiver(notificationId, u, channel));
        }

        return result;
    }

    private NotificationReceiverDO buildReceiver(Long notificationId, UserDO user, String channel) {
        NotificationReceiverDO r = new NotificationReceiverDO();
        r.setNotificationId(notificationId);
        r.setUserId(user.getId());
        r.setChannel(channel);
        r.setReadStatus("unread");
        r.setSendStatus("pending");
        if ("email".equals(channel)) {
            r.setReceiver(user.getEmail());
        } else if ("sms".equals(channel)) {
            r.setReceiver(user.getMobile());
        } else {
            r.setReceiver(String.valueOf(user.getId()));
        }
        return r;
    }

}
