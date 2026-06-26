package cn.xku.law.subscription.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.subscription.convert.SubscriptionMatchConvert;
import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import cn.xku.law.subscription.domain.dto.SubscriptionMatchQueryDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionMatchVO;
import cn.xku.law.subscription.mapper.SubscriptionMatchMapper;
import cn.xku.law.subscription.mapper.SubscriptionRuleMapper;
import cn.xku.law.subscription.service.SubscriptionMatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionMatchServiceImpl extends ServiceImpl<SubscriptionMatchMapper, SubscriptionMatchDO>
        implements SubscriptionMatchService {

    private final SubscriptionMatchConvert convert;
    private final SubscriptionRuleMapper subscriptionRuleMapper;

    @Override
    public PageResult<SubscriptionMatchVO> pageMatches(SubscriptionMatchQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Long> ownRuleIds = subscriptionRuleMapper.selectList(
                new LambdaQueryWrapper<SubscriptionRuleDO>()
                        .select(SubscriptionRuleDO::getId)
                        .eq(SubscriptionRuleDO::getUserId, userId))
                .stream().map(SubscriptionRuleDO::getId).collect(Collectors.toList());

        if (ownRuleIds.isEmpty()) {
            return PageResult.of(0L, List.of());
        }

        // 传入了不属于自己的 ruleId → 直接返回空页，不做任何检索
        if (query.getRuleId() != null && !ownRuleIds.contains(query.getRuleId())) {
            return PageResult.of(0L, List.of());
        }

        LambdaQueryWrapper<SubscriptionMatchDO> wrapper = new LambdaQueryWrapper<SubscriptionMatchDO>()
                .in(SubscriptionMatchDO::getRuleId, ownRuleIds)
                .eq(query.getRuleId() != null, SubscriptionMatchDO::getRuleId, query.getRuleId())
                .eq(StringUtils.hasText(query.getReadStatus()),
                        SubscriptionMatchDO::getReadStatus, query.getReadStatus())
                .orderByDesc(SubscriptionMatchDO::getMatchedTime);
        IPage<SubscriptionMatchDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public long markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> ownRuleIds = ownRuleIds(userId);
        if (ownRuleIds.isEmpty()) {
            return 0L;
        }
        long pending = countByUnreadFlag(ownRuleIds);
        if (pending == 0L) {
            return 0L;
        }
        SubscriptionMatchDO patch = new SubscriptionMatchDO();
        patch.setReadStatus("read");
        this.update(patch, new LambdaQueryWrapper<SubscriptionMatchDO>()
                .in(SubscriptionMatchDO::getRuleId, ownRuleIds)
                .eq(SubscriptionMatchDO::getReadStatus, "unread"));
        return pending;
    }

    @Override
    public long countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> ownRuleIds = ownRuleIds(userId);
        if (ownRuleIds.isEmpty()) {
            return 0L;
        }
        return countByUnreadFlag(ownRuleIds);
    }

    private List<Long> ownRuleIds(Long userId) {
        return subscriptionRuleMapper.selectList(
                        new LambdaQueryWrapper<SubscriptionRuleDO>()
                                .select(SubscriptionRuleDO::getId)
                                .eq(SubscriptionRuleDO::getUserId, userId))
                .stream().map(SubscriptionRuleDO::getId).collect(Collectors.toList());
    }

    private long countByUnreadFlag(List<Long> ownRuleIds) {
        return this.count(new LambdaQueryWrapper<SubscriptionMatchDO>()
                .in(SubscriptionMatchDO::getRuleId, ownRuleIds)
                .eq(SubscriptionMatchDO::getReadStatus, "unread"));
    }

    @Override
    public void markRead(Long matchId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SubscriptionMatchDO match = this.getById(matchId);
        if (match == null) return;

        SubscriptionRuleDO rule = subscriptionRuleMapper.selectById(match.getRuleId());
        if (rule == null || !currentUserId.equals(rule.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if ("unread".equals(match.getReadStatus())) {
            match.setReadStatus("read");
            this.updateById(match);
        }
    }
}
