package cn.xku.law.subscription.serviceImpl;

import cn.xku.law.common.constant.EffectLevelMapping;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.OwnerValidator;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.subscription.convert.SubscriptionRuleConvert;
import cn.xku.law.subscription.domain.DocumentMatchContext;
import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleCreateDTO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleQueryDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionRuleVO;
import cn.xku.law.subscription.mapper.SubscriptionMatchMapper;
import cn.xku.law.subscription.mapper.SubscriptionRuleMapper;
import cn.xku.law.subscription.service.AlertDeliveryService;
import cn.xku.law.subscription.service.SubscriptionRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionRuleServiceImpl extends ServiceImpl<SubscriptionRuleMapper, SubscriptionRuleDO>
        implements SubscriptionRuleService {

    private final SubscriptionRuleConvert convert;
    private final SubscriptionMatchMapper subscriptionMatchMapper;
    private final AlertDeliveryService alertDeliveryService;
    private final ObjectMapper objectMapper;

    private static final int TRIGGER_BATCH_SIZE = 100;

    @Override
    public PageResult<SubscriptionRuleVO> pageRules(SubscriptionRuleQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<SubscriptionRuleDO> wrapper = new LambdaQueryWrapper<SubscriptionRuleDO>()
                .eq(SubscriptionRuleDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getRuleType()),
                        SubscriptionRuleDO::getRuleType, query.getRuleType())
                .eq(StringUtils.hasText(query.getStatus()),
                        SubscriptionRuleDO::getStatus, query.getStatus())
                .orderByDesc(SubscriptionRuleDO::getCreateTime);
        IPage<SubscriptionRuleDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public Long createRule(SubscriptionRuleCreateDTO dto) {
        SubscriptionRuleDO entity = convert.toDO(dto);
        entity.setUserId(SecurityUtils.getCurrentUserId());
        if (!StringUtils.hasText(entity.getRuleType())) {
            entity.setRuleType("law_update");
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("enabled");
        }
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateRule(Long id, SubscriptionRuleCreateDTO dto) {
        SubscriptionRuleDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.SUBSCRIPTION_RULE_NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeRule(Long id) {
        SubscriptionRuleDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.SUBSCRIPTION_RULE_NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        this.removeById(id);
    }

    @Override
    public void triggerMatchForDocument(DocumentMatchContext ctx) {
        LocalDateTime now = LocalDateTime.now();
        long lastId = 0L;
        List<SubscriptionRuleDO> rules;

        while (!(rules = baseMapper.selectEnabledLawUpdateRulesBatch(lastId, TRIGGER_BATCH_SIZE)).isEmpty()) {
            for (SubscriptionRuleDO rule : rules) {
                try {
                    String reason = evaluate(rule, ctx);
                    if (reason == null) {
                        continue; // 规则条件未命中，不生成命中/投递，避免全量轰炸
                    }
                    SubscriptionMatchDO match = new SubscriptionMatchDO();
                    match.setRuleId(rule.getId());
                    match.setDocumentId(ctx.getDocumentId());
                    match.setVersionId(ctx.getVersionId());
                    match.setTitleSnapshot(ctx.getTitle());
                    match.setMatchType(ctx.getMatchType());
                    match.setMatchReason(reason);
                    match.setMatchedTime(now);
                    match.setReadStatus("unread");
                    subscriptionMatchMapper.insert(match);

                    alertDeliveryService.createAndDeliver(match, rule, ctx.getTitle(), reason);
                } catch (Exception e) {
                    log.warn("[SubscriptionTrigger] 规则 {} 处理失败: {}", rule.getId(), e.getMessage());
                }
            }
            lastId = rules.get(rules.size() - 1).getId();
        }
    }

    /**
     * 按规则条件评估法规是否命中，命中返回可读 matchReason，未命中返回 null。
     * 维度间 AND（关键词 / 地区 / 效力级别 / 发布机关），每个维度内 OR；规则未设该维度则跳过。
     * 关键词来自 rule.keyword 与 filtersJson.keywords；其余维度来自 filtersJson。
     */
    String evaluate(SubscriptionRuleDO rule, DocumentMatchContext ctx) {
        JsonNode filters = parseFilters(rule.getFiltersJson());
        List<String> reasons = new ArrayList<>();

        List<String> keywords = new ArrayList<>(splitTokens(rule.getKeyword()));
        keywords.addAll(jsonStrings(filters, "keywords"));
        if (!keywords.isEmpty()) {
            String haystack = (safe(ctx.getTitle()) + " " + safe(ctx.getSummary())).toLowerCase();
            String hit = keywords.stream().filter(k -> haystack.contains(k.toLowerCase())).findFirst().orElse(null);
            if (hit == null) return null;
            reasons.add("关键词『" + hit + "』命中");
        }

        List<String> regions = jsonStrings(filters, "regionCode");
        if (!regions.isEmpty()) {
            String region = safe(ctx.getRegionCode());
            String hit = regions.stream()
                    .filter(r -> StringUtils.hasText(region) && region.startsWith(r)).findFirst().orElse(null);
            if (hit == null) return null;
            reasons.add("地区匹配 " + hit);
        }

        // 复用 EffectLevelMapping 把 code 展开为中文原值集合后比对（与检索口径一致）。
        List<String> levelCodes = jsonStrings(filters, "effectLevel");
        if (!levelCodes.isEmpty()) {
            String level = safe(ctx.getLegalLevel());
            Set<String> rawLevels = levelCodes.stream()
                    .flatMap(c -> EffectLevelMapping.toRawValues(c).stream())
                    .collect(Collectors.toSet());
            if (!rawLevels.contains(level)) return null;
            reasons.add("效力级别=" + level);
        }

        // 发布机关维度：包含匹配
        List<String> authorities = jsonStrings(filters, "authority");
        if (!authorities.isEmpty()) {
            String org = safe(ctx.getIssuingOrg());
            String hit = authorities.stream()
                    .filter(a -> StringUtils.hasText(org) && org.contains(a)).findFirst().orElse(null);
            if (hit == null) return null;
            reasons.add("发布机关含『" + hit + "』");
        }

        String prefix = "法规" + zhMatchType(ctx.getMatchType());
        if (reasons.isEmpty()) {
            return prefix + "（订阅全部更新）";
        }
        return prefix + "：" + String.join("、", reasons);
    }

    private JsonNode parseFilters(String filtersJson) {
        if (!StringUtils.hasText(filtersJson)) return null;
        try {
            return objectMapper.readTree(filtersJson);
        } catch (Exception e) {
            log.warn("[SubscriptionTrigger] filtersJson 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** 取 JSON 中某字段为字符串数组；兼容单字符串值。 */
    private static List<String> jsonStrings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (node == null) return out;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return out;
        if (v.isArray()) {
            v.forEach(e -> {
                if (e != null && StringUtils.hasText(e.asText())) out.add(e.asText().trim());
            });
        } else if (StringUtils.hasText(v.asText())) {
            out.addAll(splitTokens(v.asText()));
        }
        return out;
    }

    /** 把关键词串按空白/逗号/分号拆分为去空多词。 */
    private static List<String> splitTokens(String raw) {
        List<String> out = new ArrayList<>();
        if (!StringUtils.hasText(raw)) return out;
        for (String t : raw.split("[\\s,，;；]+")) {
            if (StringUtils.hasText(t)) out.add(t.trim());
        }
        return out;
    }

    private static String zhMatchType(String matchType) {
        if (matchType == null) return "更新";
        return switch (matchType) {
            case "new" -> "新增";
            case "repeal" -> "废止";
            default -> "更新";
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
