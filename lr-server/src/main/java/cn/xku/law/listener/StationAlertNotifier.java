package cn.xku.law.listener;

import cn.xku.law.common.client.AlertNotifier;
import cn.xku.law.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AlertNotifier 的站内信实现：把订阅预警写入通知中心收件箱。
 * Bean 名 stationAlertNotifier 与 {@code NoOpAlertNotifier} 的 @ConditionalOnMissingBean 对应，
 * 装配后即覆盖 NoOp，使订阅预警走真实站内信投递。
 */
@Component("stationAlertNotifier")
@RequiredArgsConstructor
public class StationAlertNotifier implements AlertNotifier {

    private final NotificationService notificationService;

    @Override
    public boolean notifyStation(Long userId, String title, String content, String refType, Long refId) {
        return notificationService.createStationNotification(userId, title, content, refType, refId);
    }
}
