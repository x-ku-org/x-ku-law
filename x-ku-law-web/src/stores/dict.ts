import { ref } from 'vue';
import { defineStore } from 'pinia';
import { batchDictData } from '@/api/admin';
import { registerDictLabels } from '@/utils/labels';
import type { DictDataRecord } from '@/types/admin';
import type { OptionItem } from '@/types/api';

/**
 * 核心法规域字典编码，与后端 V19__dict_seed.sql / labels.ts 的 *Options 一一对应。
 * 启动后一次性批量加载，缓存于内存；任一项缺失时调用方回退到 labels.ts 的硬编码常量。
 */
export const CORE_DICT_CODES = [
  'law_type',
  'effect_level',
  'timeliness_status',
  'region',
  'version_status',
  'revision_type',
  'relation_type',
  'category_type'
] as const;

export const useDictStore = defineStore('dict', () => {
  const data = ref<Record<string, DictDataRecord[]>>({});
  const loaded = ref(false);

  let loadPromise: Promise<void> | null = null;

  /** 幂等批量加载核心字典；失败静默（保持未加载态，调用方自动回退常量）。 */
  async function ensureLoaded(force = false): Promise<void> {
    if (loaded.value && !force) return;
    loadPromise ??= batchDictData([...CORE_DICT_CODES])
      .then((map) => {
        data.value = map ?? {};
        // 合并 value→label 进 labelOf 覆盖表，使全站状态徽标也由字典驱动。
        const labels: Record<string, string> = {};
        for (const items of Object.values(data.value)) {
          for (const it of items) {
            if (it.dictValue != null && it.dictLabel) labels[it.dictValue] = it.dictLabel;
          }
        }
        registerDictLabels(labels);
        loaded.value = true;
      })
      .catch(() => {
        // 字典不可用：保持空缓存，调用方回退到内置常量。
      })
      .finally(() => {
        loadPromise = null;
      });
    return loadPromise;
  }

  /**
   * 取某字典编码的下拉选项；字典缺失/为空时返回 fallback（labels.ts 的硬编码常量）。
   */
  function options(dictCode: string, fallback: OptionItem[] = []): OptionItem[] {
    const items = data.value[dictCode];
    if (!items || !items.length) return fallback;
    return items.map((it) => ({ label: it.dictLabel ?? String(it.dictValue ?? ''), value: it.dictValue ?? '' }));
  }

  return { data, loaded, ensureLoaded, options };
});
