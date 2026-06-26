package cn.xku.law.subscription.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRuleCreateDTO {

    @NotBlank
    private String ruleName;
    private String ruleType;
    /** 关键词（可空格/逗号分隔多个），与 filtersJson.keywords 合并参与标题/摘要包含匹配 */
    private String keyword;
    /**
     * 条件过滤 JSON。维度间 AND、维度内 OR，缺省维度不约束：
     * {@code { "keywords": ["数据出境"], "regionCode": ["110000"], "effectLevel": ["行政法规"], "authority": ["国务院"] }}
     * 各字段均可为字符串或字符串数组；regionCode 支持前缀匹配（省级规则匹配市级法规）。
     */
    private String filtersJson;
    private String deliveryChannel;
    private String frequency;
    private String status;
}
