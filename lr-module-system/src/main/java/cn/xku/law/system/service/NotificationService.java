package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.NotificationDO;
import cn.xku.law.system.domain.dto.NotificationCreateDTO;
import cn.xku.law.system.domain.dto.NotificationInboxQueryDTO;
import cn.xku.law.system.domain.dto.NotificationQueryDTO;
import cn.xku.law.system.domain.vo.NotificationInboxVO;
import cn.xku.law.system.domain.vo.NotificationVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NotificationService extends IService<NotificationDO> {
    PageResult<NotificationVO> pageNotifications(NotificationQueryDTO query);
    PageResult<NotificationInboxVO> pageInbox(NotificationInboxQueryDTO query);
    Long createNotification(NotificationCreateDTO dto);
    void removeNotification(Long id);
    void markRead(Long notificationId, Long userId);

    /** 把该用户收件箱内全部未读通知标记为已读，返回更新条数 */
    long markAllRead(Long userId);

    /** 该用户收件箱内未读通知数 */
    long countUnread(Long userId);

    /**
     * 站内信实时投递给单个用户，按用户真实租户落库（用于订阅预警等无安全上下文的异步场景）。
     * 通知与接收记录 send_status 直接置 sent；用户未找到则不创建接收记录返回 false。
     */
    boolean createStationNotification(Long userId, String title, String content, String refType, Long refId);
}
