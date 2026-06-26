package cn.xku.law.subscription.service;

import cn.xku.law.subscription.domain.AlertDeliveryDO;
import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlertDeliveryService extends IService<AlertDeliveryDO> {

    /**
     * 为命中创建投递记录并立即按渠道投递：station 渠道走站内信实时落库并回写 sent，
     * 其他渠道（email/sms/webhook）暂留 pending 待网关接入（二期）。
     */
    void createAndDeliver(SubscriptionMatchDO match, SubscriptionRuleDO rule, String lawTitle, String matchReason);

    /** 重投单条失败/待发的投递记录（运维后台触发）。 */
    void retry(Long deliveryId);
}
