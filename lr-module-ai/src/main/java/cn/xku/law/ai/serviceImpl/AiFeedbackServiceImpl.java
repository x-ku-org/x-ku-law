package cn.xku.law.ai.serviceImpl;

import cn.xku.law.ai.convert.AiFeedbackConvert;
import cn.xku.law.ai.domain.AiFeedbackDO;
import cn.xku.law.ai.domain.AiMessageDO;
import cn.xku.law.ai.domain.dto.AiFeedbackCreateDTO;
import cn.xku.law.ai.domain.dto.AiFeedbackQueryDTO;
import cn.xku.law.ai.domain.vo.AiFeedbackVO;
import cn.xku.law.ai.mapper.AiFeedbackMapper;
import cn.xku.law.ai.service.AiMessageService;
import cn.xku.law.ai.service.AiFeedbackService;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiFeedbackServiceImpl extends ServiceImpl<AiFeedbackMapper, AiFeedbackDO>
        implements AiFeedbackService {

    private final AiFeedbackConvert convert;
    private final AiMessageService messageService;

    @Override
    public Long createFeedback(AiFeedbackCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiMessageDO message = messageService.getById(dto.getMessageId());
        if (message == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        if (!userId.equals(message.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        AiFeedbackDO entity = convert.toDO(dto);
        entity.setUserId(userId);
        entity.setHandledStatus("pending");
        this.save(entity);
        return entity.getId();
    }

    @Override
    public boolean toggleLike(Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiMessageDO message = messageService.getById(messageId);
        if (message == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        if (!userId.equals(message.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        AiFeedbackDO existing = this.getOne(new LambdaQueryWrapper<AiFeedbackDO>()
                .eq(AiFeedbackDO::getUserId, userId)
                .eq(AiFeedbackDO::getMessageId, messageId)
                .eq(AiFeedbackDO::getFeedbackType, "like")
                .last("LIMIT 1"));
        if (existing != null) {
            this.removeById(existing.getId()); // 已赞 → 取消
            return false;
        }
        AiFeedbackDO like = new AiFeedbackDO();
        like.setMessageId(messageId);
        like.setUserId(userId);
        like.setFeedbackType("like");
        like.setHandledStatus("closed"); // 点赞无需人工处理，直接关闭，避免污染待处理队列
        this.save(like);
        return true;
    }

    @Override
    public PageResult<AiFeedbackVO> pageFeedbacks(AiFeedbackQueryDTO query) {
        LambdaQueryWrapper<AiFeedbackDO> wrapper = new LambdaQueryWrapper<AiFeedbackDO>()
                .eq(StringUtils.hasText(query.getFeedbackType()),
                        AiFeedbackDO::getFeedbackType, query.getFeedbackType())
                .eq(StringUtils.hasText(query.getHandledStatus()),
                        AiFeedbackDO::getHandledStatus, query.getHandledStatus())
                .orderByDesc(AiFeedbackDO::getCreateTime);
        IPage<AiFeedbackDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public void handleFeedback(Long id, String handledStatus) {
        AiFeedbackDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.NOT_FOUND);
        entity.setHandledStatus(StringUtils.hasText(handledStatus) ? handledStatus : "resolved");
        entity.setHandledUserId(SecurityUtils.getCurrentUserId());
        entity.setHandledTime(LocalDateTime.now());
        this.updateById(entity);
    }
}
