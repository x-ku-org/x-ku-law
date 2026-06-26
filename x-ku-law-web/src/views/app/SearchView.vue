<template>
  <section class="page search-page">
    <SearchQueryHead
      v-model="query.keyword"
      :hit-count="total"
      :doc-count="documentCount"
      :elapsed-ms="elapsedMs"
      :stats-loading="loading"
      @submit="submitSearch"
    />

    <div class="toolbar-row">
      <span class="t-meta-cap filter-label">效力层级</span>
      <div class="level-chips">
        <button
          v-for="opt in levelChipOptions"
          :key="opt.value"
          type="button"
          class="level-chip"
          :class="{ active: query.effectLevel === opt.value }"
          @click="setEffectLevel(opt.value)"
        >
          {{ opt.label }}
        </button>
      </div>
      <div class="toolbar-extra">
        <XSelect v-model="query.status" :options="statusOptions" placeholder="时效状态" @update:model-value="onFilterChange" />
        <XSelect v-model="query.regionCode" :options="regionSelectOptions" placeholder="适用地区" @update:model-value="onFilterChange" />
        <XSelect v-model="query.sort" :options="sortSelectOptions" placeholder="排序方式" @update:model-value="onFilterChange" />
        <XInput v-model="query.publishAuthority" placeholder="发布机关" @keyup.enter="onFilterChange" />
        <XButton type="button" :loading="savingSearch" :disabled="!query.keyword" @click="saveSearch">保存检索</XButton>
      </div>
    </div>

    <div v-if="activeFilterChips.length" class="filter-summary">
      <span class="mono">已筛选</span>
      <button v-for="chip in activeFilterChips" :key="chip.key" type="button" class="filter-chip" @click="clearFilter(chip.key)">
        {{ chip.label }}
      </button>
      <XButton size="small" variant="ghost" @click="clearAllFilters">清空</XButton>
    </div>

    <SkeletonResultMatrix v-if="showMatrixSkeleton" />
    <ResultLevelMatrix
      v-else-if="showMatrix"
      :levels="matrixLevels"
      :years="matrixYears"
      :nodes="matrixNodes"
    />

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" variant="search" :count="5" />
    <EmptyState v-else-if="!items.length" title="暂无检索结果" description="尝试更换关键词、减少筛选条件或检查文号是否准确。" />
    <LawResultList v-else :items="items" @open="openLaw" />

    <XPagination
      v-if="!loading && total > 0"
      :total="total"
      :page-no="query.pageNo"
      :page-size="query.pageSize"
      :page-size-options="pageSizeOptions"
      @change="goPage"
      @size-change="changeSize"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router';
import LawResultList from '@/components/business/LawResultList.vue';
import ResultLevelMatrix from '@/components/editorial/ResultLevelMatrix.vue';
import SearchQueryHead from '@/components/editorial/SearchQueryHead.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import SkeletonResultMatrix from '@/components/common/SkeletonResultMatrix.vue';
import XButton from '@/components/common/XButton.vue';
import XInput from '@/components/common/XInput.vue';
import XPagination from '@/components/common/XPagination.vue';
import XSelect from '@/components/common/XSelect.vue';
import { searchLaws } from '@/api/law';
import { createSavedSearch } from '@/api/workspace';
import { usePreferencesStore } from '@/stores/preferences';
import { lawDetailTo } from '@/router/navigation';
import type { LawSearchResult, MatrixBucket } from '@/types/law';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';
import { effectLevelOptions as effectLevelDefs, labelOf, regionOptions, searchSortOptions, timelinessOptions } from '@/utils/labels';
import { buildMatrixFromBuckets, defaultMatrixYears } from '@/utils/searchMatrix';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const preferences = usePreferencesStore();
const loading = ref(false);
const error = ref('');
const savingSearch = ref(false);
const total = ref(0);
const documentCount = ref(0);
const matrix = ref<MatrixBucket[]>([]);
const elapsedMs = ref<number | undefined>();
const items = ref<LawSearchResult[]>([]);
const pageSizeOptions = [10, 20, 50, 100];
const query = reactive({
  keyword: '',
  effectLevel: '',
  status: '',
  publishAuthority: '',
  regionCode: '',
  sort: 'relevance',
  pageNo: 1,
  pageSize: 10
});

const levelChipOptions = [{ label: '全部', value: '' }, ...effectLevelDefs];
const statusOptions = [{ label: '全部时效状态', value: '' }, ...timelinessOptions];
const regionSelectOptions = [{ label: '全部地区', value: '' }, ...regionOptions];
const sortSelectOptions = searchSortOptions;

const matrixYears = defaultMatrixYears();
const matrixBundle = computed(() => buildMatrixFromBuckets(matrix.value, matrixYears));
const matrixLevels = computed(() => matrixBundle.value.levels);
const matrixNodes = computed(() => matrixBundle.value.nodes);
const showMatrix = computed(() => !loading.value && items.value.length > 0);
const showMatrixSkeleton = computed(
  () => loading.value && (items.value.length > 0 || matrix.value.length > 0 || !!query.keyword.trim())
);
type FilterKey = 'effectLevel' | 'status' | 'regionCode' | 'publishAuthority' | 'sort';

const FILTER_DEFS: ReadonlyArray<{ key: FilterKey; prefix: string; reset: string; display: (v: string) => string }> = [
  { key: 'effectLevel', prefix: '层级', reset: '', display: labelOf },
  { key: 'status', prefix: '时效', reset: '', display: labelOf },
  { key: 'regionCode', prefix: '地区', reset: '', display: labelOf },
  { key: 'publishAuthority', prefix: '机关', reset: '', display: (v) => v },
  { key: 'sort', prefix: '排序', reset: 'relevance', display: labelOf }
];

const activeFilterChips = computed(() =>
  FILTER_DEFS.filter((def) => query[def.key] && query[def.key] !== def.reset).map((def) => ({
    key: def.key,
    label: `${def.prefix}：${def.display(query[def.key])}`
  }))
);

function applyRoute() {
  query.keyword = String(route.query.keyword || '');
  query.effectLevel = String(route.query.effectLevel || '');
  query.status = String(route.query.status || '');
  query.publishAuthority = String(route.query.publishAuthority || '');
  query.regionCode = String(route.query.regionCode || '');
  query.sort = String(route.query.sort || 'relevance');
  const pageNo = Number(route.query.pageNo);
  query.pageNo = Number.isFinite(pageNo) && pageNo > 0 ? pageNo : 1;
  const pageSize = Number(route.query.pageSize);
  query.pageSize = pageSizeOptions.includes(pageSize) ? pageSize : 10;
}

function buildQuery(): LocationQueryRaw {
  const q: LocationQueryRaw = {};
  if (query.keyword) q.keyword = query.keyword;
  if (query.effectLevel) q.effectLevel = query.effectLevel;
  if (query.status) q.status = query.status;
  if (query.publishAuthority) q.publishAuthority = query.publishAuthority;
  if (query.regionCode) q.regionCode = query.regionCode;
  if (query.sort && query.sort !== 'relevance') q.sort = query.sort;
  if (query.pageNo > 1) q.pageNo = String(query.pageNo);
  if (query.pageSize !== 10) q.pageSize = String(query.pageSize);
  return q;
}

function pushQuery() {
  router.push({ query: buildQuery() });
}

function setEffectLevel(value: string) {
  query.effectLevel = value;
  query.pageNo = 1;
  pushQuery();
}


function onFilterChange() {
  query.pageNo = 1;
  void nextTick(() => pushQuery());
}

async function runSearch() {
  loading.value = true;
  error.value = '';
  const started = performance.now();
  try {
    const result = await searchLaws(query);
    items.value = result.list;
    total.value = result.total;
    documentCount.value = result.documentCount ?? 0;
    matrix.value = result.matrix ?? [];
    elapsedMs.value = performance.now() - started;
  } catch (err) {
    error.value = resolveApiError(err, '检索失败。');
    items.value = [];
    total.value = 0;
    documentCount.value = 0;
    matrix.value = [];
    elapsedMs.value = undefined;
  } finally {
    loading.value = false;
  }
}

function submitSearch() {
  query.pageNo = 1;
  pushQuery();
}

async function saveSearch() {
  if (!query.keyword) {
    toast.info('请先输入关键词。');
    return;
  }
  try {
    savingSearch.value = true;
    await createSavedSearch({
      name: `${query.keyword} 检索`,
      keyword: query.keyword,
      filtersJson: JSON.stringify({
        effectLevel: query.effectLevel,
        status: query.status,
        publishAuthority: query.publishAuthority,
        regionCode: query.regionCode,
        sort: query.sort
      }),
      notifyEnabled: false,
      status: 'enabled'
    });
    toast.success('已保存检索条件。');
  } catch (err) {
    toast.error(resolveApiError(err, '保存检索失败。'));
  } finally {
    savingSearch.value = false;
  }
}

function clearFilter(key: FilterKey) {
  query[key] = FILTER_DEFS.find((def) => def.key === key)?.reset ?? '';
  query.pageNo = 1;
  pushQuery();
}

function clearAllFilters() {
  for (const def of FILTER_DEFS) query[def.key] = def.reset;
  query.pageNo = 1;
  pushQuery();
}

function openLaw(item: LawSearchResult) {
  router.push(lawDetailTo(item.documentId));
}

function goPage(pageNo: number) {
  if (pageNo === query.pageNo) return;
  query.pageNo = pageNo;
  pushQuery();
}

function changeSize(pageSize: number) {
  if (pageSize === query.pageSize) return;
  query.pageSize = pageSize;
  query.pageNo = 1;
  pushQuery();
}

watch(
  () => route.query,
  () => {
    applyRoute();
    void runSearch();
  }
);

/**
 * 首次进入时套用用户「检索默认值」偏好：仅对 URL 中缺失的参数生效，
 * 已带条件的分享/直链一律以 URL 为准，不被覆盖。后续路由变更只走 watch（不再叠加默认值）。
 */
async function applyUserDefaults() {
  await preferences.ensureLoaded();
  const d = preferences.searchDefaults;
  if (route.query.sort === undefined && d.sort) query.sort = d.sort;
  if (route.query.pageSize === undefined && d.pageSize && pageSizeOptions.includes(d.pageSize)) {
    query.pageSize = d.pageSize;
  }
  if (route.query.regionCode === undefined && d.regionCode) query.regionCode = d.regionCode;
  if (route.query.effectLevel === undefined && d.effectLevel) query.effectLevel = d.effectLevel;
  if (route.query.status === undefined && d.status) query.status = d.status;
}

onMounted(async () => {
  applyRoute();
  await applyUserDefaults();
  void runSearch();
});
</script>

<style scoped>
.search-page {
  display: grid;
  gap: 20px;
}

.toolbar-row {
  display: grid;
  gap: 12px;
  margin-bottom: 8px;
}

.filter-label {
  margin-bottom: 4px;
}

.level-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.level-chip {
  height: 24px;
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: transparent;
  color: var(--ink-2);
  font-family: var(--sans);
  font-size: var(--font-xs);
  cursor: pointer;
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease);
}

.level-chip:hover {
  border-color: var(--ink);
}

.level-chip.active {
  border-color: var(--accent);
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.toolbar-extra {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr)) auto;
  gap: 12px;
  align-items: stretch;
}

.filter-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
}

.filter-summary > .mono {
  color: var(--muted);
  font-size: var(--font-xs);
}

.filter-chip {
  height: 24px;
  padding: 0 10px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink-2);
  font-size: var(--font-xs);
  cursor: pointer;
}

.filter-chip:hover {
  border-color: var(--rose);
  color: var(--rose);
}

@media (max-width: 960px) {
  .toolbar-extra {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .toolbar-extra {
    grid-template-columns: 1fr;
  }
}
</style>
