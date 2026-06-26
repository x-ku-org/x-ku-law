import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { getPreferences, savePreferences } from '@/api/account';
import { getSession } from '@/api/token';
import type { SearchDefaults } from '@/types/account';

/** 检索默认值各项的偏好 key（与后端 group=search 对应）。 */
const SEARCH_KEYS = {
  sort: 'search.sort',
  pageSize: 'search.pageSize',
  regionCode: 'search.regionCode',
  effectLevel: 'search.effectLevel',
  status: 'search.status'
} as const;

/**
 * 用户个性化偏好（lr_user_preference）。当前承载「检索默认值」，
 * 以通用 key→value 字典存取，便于后续扩展通知/AI 偏好而无需改 store 形状。
 */
export const usePreferencesStore = defineStore('preferences', () => {
  const raw = ref<Record<string, string>>({});
  const loaded = ref(false);

  const searchDefaults = computed<SearchDefaults>(() => ({
    sort: raw.value[SEARCH_KEYS.sort] || '',
    pageSize: raw.value[SEARCH_KEYS.pageSize] ? Number(raw.value[SEARCH_KEYS.pageSize]) : undefined,
    regionCode: raw.value[SEARCH_KEYS.regionCode] || '',
    effectLevel: raw.value[SEARCH_KEYS.effectLevel] || '',
    status: raw.value[SEARCH_KEYS.status] || ''
  }));

  let loadPromise: Promise<void> | null = null;
  /** 幂等加载；未登录直接清空。参考 auth.ensureProfile 的写法。 */
  async function ensureLoaded(force = false) {
    if (!getSession()) {
      raw.value = {};
      loaded.value = false;
      return;
    }
    if (loaded.value && !force) return;
    loadPromise ??= getPreferences()
      .then((map) => {
        raw.value = map || {};
        loaded.value = true;
      })
      .catch(() => {
        /* 偏好加载失败不阻断主流程，检索退回内置默认值 */
      })
      .finally(() => {
        loadPromise = null;
      });
    return loadPromise;
  }

  async function saveSearchDefaults(value: SearchDefaults) {
    const kv: Record<string, string> = {
      [SEARCH_KEYS.sort]: value.sort ?? '',
      [SEARCH_KEYS.pageSize]: value.pageSize != null ? String(value.pageSize) : '',
      [SEARCH_KEYS.regionCode]: value.regionCode ?? '',
      [SEARCH_KEYS.effectLevel]: value.effectLevel ?? '',
      [SEARCH_KEYS.status]: value.status ?? ''
    };
    await savePreferences(kv);
    raw.value = { ...raw.value, ...kv };
    loaded.value = true;
  }

  function reset() {
    raw.value = {};
    loaded.value = false;
  }

  return { searchDefaults, ensureLoaded, saveSearchDefaults, reset };
});
