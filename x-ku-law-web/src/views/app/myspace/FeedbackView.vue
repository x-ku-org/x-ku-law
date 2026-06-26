<template>
  <div class="ms-pane">
    <header class="ms-pane__head">
      <div>
        <h2 class="ms-pane__title">我的反馈</h2>
        <p>提交法规纠错、检索与 AI 问题，并跟踪处理进度。</p>
      </div>
      <XButton variant="primary" @click="openCreate">新建反馈</XButton>
    </header>

    <div class="ms-toolbar">
      <XSelect v-model="typeFilter" :options="typeFilterOptions" placeholder="全部类型" />
      <XSelect v-model="statusFilter" :options="statusFilterOptions" placeholder="全部状态" />
      <XButton :loading="loading" @click="reload(1)">筛选</XButton>
    </div>

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" :count="4" />
    <EmptyState
      v-else-if="!rows.length"
      title="还没有反馈"
      description="遇到法规数据、检索或 AI 回答的问题，欢迎反馈给我们。"
    >
      <XButton variant="primary" @click="openCreate">提交第一条反馈</XButton>
    </EmptyState>

    <ol v-else class="fb-list">
      <li v-for="fb in rows" :key="fb.id" class="fb-item">
        <div class="fb-item__rail">
          <span class="fb-item__dot" :class="toneClass(fb.status)" />
        </div>
        <div class="fb-item__body">
          <div class="fb-item__meta">
            <XChip tone="outline">{{ labelOf(fb.feedbackType) }}</XChip>
            <StatusBadge :value="fb.status" />
            <span class="mono fb-item__time">{{ formatDateTime(fb.createTime) }}</span>
          </div>
          <p class="fb-item__content">{{ fb.content }}</p>
        </div>
        <XButton size="small" variant="ghost" @click="remove(fb)">删除</XButton>
      </li>
    </ol>

    <XPagination v-if="rows.length" class="ms-pager" :total="total" :page-no="pageNo" :page-size="pageSize" @change="reload" />

    <XModal :open="createOpen" title="新建反馈" kicker="§ Feedback" max-width="560px" @update:open="createOpen = $event">
      <form id="fb-form" class="fb-form" @submit.prevent="submit">
        <XFormField label="反馈类型" required :error="errors.feedbackType">
          <XSelect v-model="draft.feedbackType" :options="typeOptions" placeholder="请选择" :invalid="Boolean(errors.feedbackType)" />
        </XFormField>
        <XFormField label="反馈内容" required :error="errors.content">
          <XTextarea v-model="draft.content" placeholder="请描述问题或建议…" :invalid="Boolean(errors.content)" />
        </XFormField>
      </form>
      <template #footer>
        <span v-if="formMessage" class="form-message">{{ formMessage }}</span>
        <XButton type="button" variant="ghost" @click="createOpen = false">取消</XButton>
        <XButton variant="primary" type="submit" form="fb-form" :loading="submitting">提交</XButton>
      </template>
    </XModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import XFormField from '@/components/common/XFormField.vue';
import XModal from '@/components/common/XModal.vue';
import XPagination from '@/components/common/XPagination.vue';
import XSelect from '@/components/common/XSelect.vue';
import XTextarea from '@/components/common/XTextarea.vue';
import type { OptionItem } from '@/types/api';
import type { Feedback } from '@/types/workspace';
import { createFeedback, deleteFeedback, getFeedbacks } from '@/api/workspace';
import { formatDateTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { resolveApiError } from '@/utils/apiError';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';

const { confirm } = useConfirm();
const toast = useToast();

const typeOptions: OptionItem[] = [
  { label: '数据纠错', value: 'data_error' },
  { label: '检索问题', value: 'search_error' },
  { label: 'AI 回答问题', value: 'ai_error' },
  { label: '功能问题', value: 'function' },
  { label: '建议', value: 'suggestion' }
];
const typeFilterOptions: OptionItem[] = [{ label: '全部类型', value: '' }, ...typeOptions];
const statusFilterOptions: OptionItem[] = [
  { label: '全部状态', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已处理', value: 'resolved' },
  { label: '已关闭', value: 'closed' }
];

const rows = ref<Feedback[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const typeFilter = ref('');
const statusFilter = ref('');
const loading = ref(false);
const error = ref('');

const createOpen = ref(false);
const draft = reactive({ feedbackType: '', content: '' });
const errors = reactive<{ feedbackType?: string; content?: string }>({});
const formMessage = ref('');
const submitting = ref(false);

function toneClass(status?: string) {
  const s = (status || 'pending').toLowerCase();
  if (s === 'resolved' || s === 'done' || s === 'pass') return 'done';
  if (s === 'processing') return 'doing';
  if (s === 'closed' || s === 'rejected') return 'closed';
  return 'pending';
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getFeedbacks({
      pageNo: pageNo.value,
      pageSize,
      feedbackType: typeFilter.value || undefined,
      status: statusFilter.value || undefined
    });
    rows.value = res.list;
    total.value = res.total;
  } catch (err) {
    error.value = resolveApiError(err, '反馈读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  draft.feedbackType = '';
  draft.content = '';
  errors.feedbackType = '';
  errors.content = '';
  formMessage.value = '';
  createOpen.value = true;
}

async function submit() {
  errors.feedbackType = draft.feedbackType ? '' : '请选择反馈类型。';
  errors.content = draft.content.trim() ? '' : '反馈内容不能为空。';
  if (errors.feedbackType || errors.content) return;
  submitting.value = true;
  try {
    await createFeedback({ feedbackType: draft.feedbackType, content: draft.content.trim() });
    toast.success('反馈已提交，感谢！');
    createOpen.value = false;
    await reload(1);
  } catch (err) {
    formMessage.value = resolveApiError(err, '提交失败。');
  } finally {
    submitting.value = false;
  }
}

async function remove(fb: Feedback) {
  const ok = await confirm({
    title: '删除这条反馈？',
    message: '删除后无法恢复。',
    confirmText: '确认删除',
    danger: true
  });
  if (!ok) return;
  try {
    await deleteFeedback(fb.id);
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

.ms-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ms-toolbar :deep(.x-select) {
  max-width: 180px;
}

.fb-list {
  margin: 0;
  padding: 0;
  list-style: none;
  border-top: 1px solid var(--ink);
}

.fb-item {
  display: grid;
  grid-template-columns: 18px 1fr auto;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--rule);
}

.fb-item__rail {
  display: flex;
  justify-content: center;
  padding-top: 5px;
}

.fb-item__dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--muted-2);
}

.fb-item__dot.pending {
  background: var(--gold);
}

.fb-item__dot.doing {
  background: var(--accent);
}

.fb-item__dot.done {
  background: var(--moss);
}

.fb-item__dot.closed {
  background: var(--muted-2);
}

.fb-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.fb-item__time {
  color: var(--muted);
  font-size: var(--font-xs);
}

.fb-item__content {
  margin: 0;
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.6;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.fb-form {
  display: grid;
  gap: 16px;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}
</style>
