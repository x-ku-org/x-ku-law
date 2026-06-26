package cn.xku.law.subscription.serviceImpl;

import cn.xku.law.common.client.AlertNotifier;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.subscription.domain.AlertDeliveryDO;
import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import cn.xku.law.subscription.mapper.AlertDeliveryMapper;
import cn.xku.law.subscription.mapper.SubscriptionMatchMapper;
import cn.xku.law.subscription.mapper.SubscriptionRuleMapper;
import cn.xku.law.subscription.service.AlertDeliveryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertDeliveryServiceImpl extends ServiceImpl<AlertDeliveryMapper, AlertDeliveryDO>
        implements AlertDeliveryService {

    private final AlertNotifier alertNotifier;
    private final SubscriptionMatchMapper subscriptionMatchMapper;
    private final SubscriptionRuleMapper subscriptionRuleMapper;

    @Override
    public void createAndDeliver(SubscriptionMatchDO match, SubscriptionRuleDO rule,
                                 String lawTitle, String matchReason) {
        String channel = StringUtils.hasText(rule.getDeliveryChannel()) ? rule.getDeliveryChannel() : "station";
        AlertDeliveryDO delivery = new AlertDeliveryDO();
        delivery.setRuleId(rule.getId());
        delivery.setMatchId(match.getId());
        delivery.setUserId(rule.getUserId());
        delivery.setChannel(channel);
        delivery.setReceiver(String.valueOf(rule.getUserId()));
        delivery.setSendStatus("pending");
        delivery.setRetryCount(0);
        this.save(delivery);

        deliver(delivery, rule.getUserId(), lawTitle, matchReason);
    }

    @Override
    public void retry(Long deliveryId) {
        AlertDeliveryDO delivery = this.getById(deliveryId);
        if (delivery == null) throw new AppException(ErrorCode.NOT_FOUND);
        if ("sent".equals(delivery.getSendStatus())) return;
        // 重投时已无发布上下文，从命中记录补全标题；取不到则用通用文案。
        String lawTitle = null;
        if (delivery.getMatchId() != null) {
            SubscriptionMatchDO match = subscriptionMatchMapper.selectById(delivery.getMatchId());
            if (match != null && match.getMatchReason() != null) {
                lawTitle = "订阅命中#" + match.getId();
            }
        }
        String reason = null;
        SubscriptionRuleDO rule = subscriptionRuleMapper.selectById(delivery.getRuleId());
        deliver(delivery, delivery.getUserId(),
                lawTitle != null ? lawTitle : "您订阅的法规有更新",
                rule != null ? "命中规则[" + rule.getRuleName() + "]" : reason);
    }

    /** 按渠道投递并回写状态。station 实时落库；其他渠道保持 pending（网关二期）。 */
    private void deliver(AlertDeliveryDO delivery, Long userId, String lawTitle, String matchReason) {
        if (!"station".equals(delivery.getChannel())) {
            // email/sms/webhook 待网关接入，保持 pending
            return;
        }
        try {
            String title = "法规订阅提醒：" + safe(lawTitle);
            StringBuilder content = new StringBuilder("您订阅的规则命中法规《").append(safe(lawTitle)).append("》。");
            if (StringUtils.hasText(matchReason)) {
                content.append(matchReason);
            }
            boolean ok = alertNotifier.notifyStation(userId, title, content.toString(),
                    "subscription_match", delivery.getMatchId());
            if (ok) {
                delivery.setSendStatus("sent");
                delivery.setSendTime(LocalDateTime.now());
                delivery.setFailReason(null);
            } else {
                delivery.setSendStatus("pending"); // 站内信通道未配置，保持待发
            }
            this.updateById(delivery);
        } catch (Exception e) {
            log.warn("[AlertDelivery] 站内信投递失败 deliveryId={}: {}", delivery.getId(), e.getMessage());
            delivery.setSendStatus("failed");
            delivery.setFailReason(truncate(e.getMessage()));
            delivery.setRetryCount(delivery.getRetryCount() == null ? 1 : delivery.getRetryCount() + 1);
            this.updateById(delivery);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}
