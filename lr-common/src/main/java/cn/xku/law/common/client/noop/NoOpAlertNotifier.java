package cn.xku.law.common.client.noop;

import cn.xku.law.common.client.AlertNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** AlertNotifier 空实现，未提供站内信投递实现时自动激活，返回 false 使投递保持 pending。 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "stationAlertNotifier")
public class NoOpAlertNotifier implements AlertNotifier {

    @Override
    public boolean notifyStation(Long userId, String title, String content, String refType, Long refId) {
        log.warn("[NoOpAlertNotifier] notifyStation called — 站内信投递未配置，保持 pending。userId={}, refType={}, refId={}",
                userId, refType, refId);
        return false;
    }
}
