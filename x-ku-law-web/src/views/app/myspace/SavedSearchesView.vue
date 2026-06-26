<template>
  <div class="ms-pane">
    <header class="ms-pane__head">
      <div>
        <h2 class="ms-pane__title">保存检索</h2>
        <p>把常用的检索条件存下来，一键回到同一组法规筛选。</p>
      </div>
      <XButton variant="primary" @click="openCreate">新建检索</XButton>
    </header>

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" :count="3" />
    <EmptyState
      v-else-if="!rows.length"
      title="还没有保存的检索"
      description="在检索页点「保存检索」，或在这里手动创建一条。"
    >
      <XButton variant="primary" @click="goSearch">去检索法规</XButton>
    </EmptyState>

    <div v-else class="ss-grid">
      <article v-for="s in rows" :key="s.id" class="ss-card">
        <div class="ss-card__top">
          <h3 class="ss-card__name">{{ s.name || `检索 #${s.id}` }}</h3>
          <XChip v-if="s.notifyEnabled" tone="gold">预警开</XChip>
        </div>
        <p class="ss-card__kw">
          <span class="ss-card__tag mono">关键词</span>
          <strong>{{ s.keyword || '（不限）' }}</strong>
        </p>
        <div v-if="filterChips(s).length" class="ss-card__chips">
          <XChip v-for="c in filterChips(s)" :key="c" tone="outline">{{ c }}</XChip>
        </div>
        <div class="ss-card__foot">
          <span class="mono">{{ formatDateTime(s.createTime) }}</span>
        </div>
        <div class="ss-card__actions">
          <XButton size="small" variant="primary" @click="run(s)">运行检索 →</XButton>
          <span class="ss-card__spacer" />
          <XButton size="small" variant="ghost" @click="remove(s)">删除</XButton>
        </div>
      </article>
    </div>

    <XPagination v-if="rows.length" class="ms-pager" :total="total" :page-no="pageNo" :page-size="pageSize" @change="reload" />

    <XModal :open="createOpen" title="新建保存检索" kicker="§ Saved Search" max-width="520px" @update:open="createOpen = $event">
      <form id="ss-form" class="ss-form" @submit.prevent="submit">
        <XFormField label="名称" required :error="nameError">
          <XInput v-model="draft.name" placeholder="如：环保法规 · 国家级" :invalid="Boolean(nameError)" />
        </XFormField>
        <XFormField label="关键词" hint="留空表示仅按下方条件筛选">
          <XInput v-model="draft.keyword" placeholder="如：环境保护" />
        </XFormField>
        <XCheckbox v-model="draft.notifyEnabled" label="命中变化时推送预警" />
      </form>
      <template #footer>
        <span v-if="formMessage" class="form-message">{{ formMessage }}</span>
        <XButton type="button" variant="ghost" @click="createOpen = false">取消</XButton>
        <XButton variant="primary" type="submit" form="ss-form" :loading="submitting">保存</XButton>
      </template>
    </XModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter, type LocationQueryRaw } from 'vue-router';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import XButton from '@/components/common/XButton.vue';
import XCheckbox from '@/components/common/XCheckbox.vue';
import XChip from '@/components/common/XChip.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XPagination from '@/components/common/XPagination.vue';
import type { SavedSearch } from '@/types/workspace';
import { createSavedSearch, deleteSavedSearch, getSavedSearches } from '@/api/workspace';
import { formatDateTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { lawSearchTo } from '@/router/navigation';
import { resolveApiError } from '@/utils/apiError';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { confirm } = useConfirm();
const toast = useToast();

interface SavedFilters {
  effectLevel?: string;
  status?: string;
  publishAuthority?: string;
  regionCode?: string;
  sort?: string;
}

const rows = ref<SavedSearch[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const loading = ref(false);
const error = ref('');

const createOpen = ref(false);
const draft = reactive({ name: '', keyword: '', notifyEnabled: false });
const nameError = ref('');
const formMessage = ref('');
const submitting = ref(false);

function parseFilters(json?: string): SavedFilters {
  if (!json || !json.trim()) return {};
  try {
    return JSON.parse(json) as SavedFilters;
  } catch {
    return {};
  }
}

function filterChips(s: SavedSearch): string[] {
  const f = parseFilters(s.filtersJson);
  const chips: string[] = [];
  if (f.effectLevel) chips.push(`效力：${labelOf(f.effectLevel)}`);
  if (f.status) chips.push(`时效：${labelOf(f.status)}`);
  if (f.regionCode) chips.push(`地区：${f.regionCode}`);
  if (f.publishAuthority) chips.push(`机关：${f.publishAuthority}`);
  if (f.sort && f.sort !== 'relevance') chips.push(`排序：${labelOf(f.sort)}`);
  return chips;
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getSavedSearches({ pageNo: pageNo.value, pageSize });
    rows.value = res.list;
    total.value = res.total;
  } catch (err) {
    error.value = resolveApiError(err, '保存检索读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function run(s: SavedSearch) {
  const f = parseFilters(s.filtersJson);
  const query: LocationQueryRaw = {};
  if (s.keyword) query.keyword = s.keyword;
  if (f.effectLevel) query.effectLevel = f.effectLevel;
  if (f.status) query.status = f.status;
  if (f.publishAuthority) query.publishAuthority = f.publishAuthority;
  if (f.regionCode) query.regionCode = f.regionCode;
  if (f.sort && f.sort !== 'relevance') query.sort = f.sort;
  router.push({ name: 'law.search', query });
}

function goSearch() {
  router.push(lawSearchTo());
}

function openCreate() {
  draft.name = '';
  draft.keyword = '';
  draft.notifyEnabled = false;
  nameError.value = '';
  formMessage.value = '';
  createOpen.value = true;
}

async function submit() {
  nameError.value = '';
  if (!draft.name.trim()) {
    nameError.value = '名称不能为空。';
    return;
  }
  submitting.value = true;
  try {
    await createSavedSearch({
      name: draft.name.trim(),
      keyword: draft.keyword.trim() || undefined,
      filtersJson: '',
      notifyEnabled: draft.notifyEnabled,
      status: 'enabled'
    });
    toast.success('已保存检索。');
    createOpen.value = false;
    await reload(1);
  } catch (err) {
    formMessage.value = resolveApiError(err, '保存失败。');
  } finally {
    submitting.value = false;
  }
}

async function remove(s: SavedSearch) {
  const ok = await confirm({
    title: '删除这条保存检索？',
    message: '删除后需要重新保存。',
    confirmText: '确认删除',
    danger: true
  });
  if (!ok) return;
  try {
    await deleteSavedSearch(s.id);
    toast.success('已删除。');
    await reload(pageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '删除失败。'));
  }
}

onMounted(() => reload(1));
</script>

<style scoped>
.ms-pane {
  display: grid;
  gap: 16px;
}

.ms-pane__head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.ms-pane__title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 24px;
  font-weight: 400;
  color: var(--ink);
}

.ms-pane__head p {
  margin: 6px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 14px;
}

.ss-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.ss-card {
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper-card);
}

.ss-card__top {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.ss-card__name {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 19px;
  font-weight: 400;
  color: var(--ink);
}

.ss-card__kw {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: baseline;
  margin: 0;
  font-size: 14px;
  color: var(--ink-2);
}

.ss-card__tag {
  color: var(--muted);
  font-size: var(--font-xxs);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.ss-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.ss-card__foot {
  color: var(--muted);
  font-size: var(--font-xs);
}

.ss-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 6px;
  border-top: 1px solid var(--rule);
}

.ss-card__spacer {
  flex: 1;
}

.ss-form {
  display: grid;
  gap: 16px;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 860px) {
  .ss-grid {
    grid-template-columns: 1fr;
  }
}
</style>
