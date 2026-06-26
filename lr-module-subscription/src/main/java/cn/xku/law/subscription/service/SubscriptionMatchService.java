package cn.xku.law.subscription.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.dto.SubscriptionMatchQueryDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionMatchVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SubscriptionMatchService extends IService<SubscriptionMatchDO> {

    PageResult<SubscriptionMatchVO> pageMatches(SubscriptionMatchQueryDTO query);

    /** 标记命中记录为已读 */
    void markRead(Long matchId);

    /** 把当前用户名下全部未读命中标记为已读，返回更新条数 */
    long markAllRead();

    /** 当前用户名下未读命中数 */
    long countUnread();
}
