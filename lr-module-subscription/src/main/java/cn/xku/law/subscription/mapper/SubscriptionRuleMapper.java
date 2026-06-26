package cn.xku.law.subscription.mapper;

import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** lr_subscription_rule 数据访问层 */
@Mapper
public interface SubscriptionRuleMapper extends BaseMapper<SubscriptionRuleDO> {

    /**
     * 游标分批查询启用的 law_update 规则，供 triggerMatchForDocument 循环调用。
     * 使用 id > lastId ORDER BY id 保证游标稳定不重不漏。
     * 绕过 TenantLine，跨租户全量触发；调用方负责按 rule.userId 写入正确 tenant_id。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM lr_subscription_rule WHERE rule_type = 'law_update' AND status = 'enabled' AND deleted = 0 AND id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<SubscriptionRuleDO> selectEnabledLawUpdateRulesBatch(@Param("lastId") Long lastId, @Param("batchSize") int batchSize);
}
