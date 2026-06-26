package cn.xku.law.subscription.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.subscription.domain.DocumentMatchContext;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleCreateDTO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleQueryDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionRuleVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SubscriptionRuleService extends IService<SubscriptionRuleDO> {

    PageResult<SubscriptionRuleVO> pageRules(SubscriptionRuleQueryDTO query);

    Long createRule(SubscriptionRuleCreateDTO dto);

    void updateRule(Long id, SubscriptionRuleCreateDTO dto);

    void removeRule(Long id);

    /**
     * 法规发布后按规则条件（关键词/地区/效力级别/发布机关）匹配，仅对命中规则生成命中记录与预警投递。
     */
    void triggerMatchForDocument(DocumentMatchContext ctx);
}
