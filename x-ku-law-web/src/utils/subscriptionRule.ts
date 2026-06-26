import type { SubscriptionRulePayload } from '@/types/workspace';

/** 订阅规则的条件过滤，序列化进 filtersJson。维度间 AND，维度内 OR。 */
export interface RuleFilters {
  effectLevel?: string[];
  regionCode?: string[];
  authority?: string[];
  keywords?: string[];
}

/** 表单展开后的扁平条件字段（与 SubscriptionRuleForm 的 v-model 对应）。 */
export interface RuleFormFilters {
  effectLevel: string;
  regionCode: string;
  authority: string;
}

/** 把扁平的表单字段合并回 filtersJson；空维度不写入。 */
export function composeSubscriptionPayload(
  base: Omit<SubscriptionRulePayload, 'filtersJson'>,
  filters: RuleFormFilters
): SubscriptionRulePayload {
  const f: RuleFilters = {};
  if (filters.effectLevel) f.effectLevel = [filters.effectLevel];
  if (filters.regionCode) f.regionCode = [filters.regionCode];
  if (filters.authority.trim()) f.authority = [filters.authority.trim()];
  return { ...base, filtersJson: Object.keys(f).length ? JSON.stringify(f) : '' };
}

/** 解析 filtersJson；非法 JSON 容错为空。 */
export function parseRuleFilters(filtersJson?: string): RuleFilters {
  if (!filtersJson || !filtersJson.trim()) return {};
  try {
    return JSON.parse(filtersJson) as RuleFilters;
  } catch {
    return {};
  }
}

/** 取条件首值，回填表单（当前每维度仅支持单值）。 */
export function ruleFiltersToForm(filtersJson?: string): RuleFormFilters {
  const f = parseRuleFilters(filtersJson);
  return {
    effectLevel: f.effectLevel?.[0] ?? '',
    regionCode: f.regionCode?.[0] ?? '',
    authority: f.authority?.[0] ?? ''
  };
}

/** 关键词字段拆成 chips：支持空格 / 逗号（中英文）分隔。 */
export function splitKeywords(keyword?: string): string[] {
  if (!keyword) return [];
  return keyword
    .split(/[\s,，、]+/)
    .map((k) => k.trim())
    .filter(Boolean);
}
