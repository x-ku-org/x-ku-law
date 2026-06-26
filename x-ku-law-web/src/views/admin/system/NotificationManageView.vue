<template>
  <section class="page notif-page">
    <AdminPageHeader kicker="§ Notifications" title="通知管理" description="撰写并广播平台通知，右侧追踪已发送记录。" />

    <div class="notif-workbench">
      <aside class="composer">
        <div class="section-kicker">撰写通知</div>
        <form class="composer-form" @submit.prevent="publish">
          <XFormField label="标题" required :error="errors.title">
            <XInput v-model="form.title" placeholder="一句话说明通知主题" :invalid="Boolean(errors.title)" />
          </XFormField>
          <XFormField label="内容" required :error="errors.content">
            <XTextarea v-model="form.content" class="composer-content" placeholder="通知正文…" :invalid="Boolean(errors.content)" />
          </XFormField>
          <XFormField label="发送范围">
            <XSelect v-model="form.sendScope" :options="sendScopeOptions" />
          </XFormField>
          <XFormField v-if="form.sendScope === 'single'" label="目标用户" required :error="errors.target">
            <XRelationSelect
              :model-value="form.targetUserId"
              resource="users"
              placeholder="搜索并选择用户…"
              :invalid="Boolean(errors.target)"
              @update:model-value="form.targetUserId = $event"
            />
          </XFormField>
          <XFormField v-if="form.sendScope === 'role'" label="目标角色" required :error="errors.target">
            <XRelationSelect
              :model-value="form.targetRoleId"
              resource="roles"
              placeholder="搜索并选择角色…"
              :invalid="Boolean(errors.target)"
              @update:model-value="form.targetRoleId = $event"
            />
          </XFormField>
          <XFormField label="模板编码" hint="可选，关联站内信模板。">
            <XInput v-model="form.templateCode" placeholder="可留空" />
          </XFormField>
          <div class="composer-foot">
            <span v-if="formMessage" class="form-message">{{ formMessage }}</span>
            <XButton variant="primary" type="submit" :loading="publishing">发布通知</XButton>
          </div>
        </form>
      </aside>

      <main class="sent">
        <div class="sent-head">
          <div class="section-kicker">已发布</div>
          <span v-if="!loading" class="mono">共 {{ total }} 条</span>
        </div>

        <PageState v-if="error" :error="error" />
        <SkeletonList v-else-if="loading" :count="5" />
        <EmptyState
          v-else-if="!rows.length"
          title="还没有发布过通知"
          description="在左侧撰写并发布第一条通知。"
        />
        <ul v-else class="notif-list">
          <li v-for="item in rows" :key="item.id" class="notif-card">
            <div class="notif-card__meta">
              <XChip v-if="item.notificationType" tone="outline">{{ labelOf(item.notificationType) }}</XChip>
              <XChip tone="accent">{{ scopeLabel(item.sendScope) }}</XChip>
              <StatusBadge :value="item.status || ''" />
              <span class="mono notif-card__time">{{ formatDateTime(item.sendTime || item.createTime) }}</span>
            </div>
            <h3 class="notif-card__title">{{ item.title || '未命名通知' }}</h3>
            <p v-if="item.content" class="notif-card__body">{{ item.content }}</p>
            <div class="notif-card__actions">
              <XButton size="small" variant="ghost" @click="remove(item.id)">删除</XButton>
            </div>
          </li>
        </ul>

        <XPagination
          class="notif-pager"
          :class="{ 'notif-pager--loading': loading }"
          :total="total"
          :page-no="pageNo"
          :page-size="pageSize"
          @change="reload"
        />
      </main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import AdminPageHeader from '@/components/business/AdminPageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XPagination from '@/components/common/XPagination.vue';
import XRelationSelect from '@/components/common/XRelationSelect.vue';
import XSelect from '@/components/common/XSelect.vue';
import XTextarea from '@/components/common/XTextarea.vue';
import { createNotification, deleteNotification, pageNotifications } from '@/api/admin';
import type { NotificationRecord } from '@/types/admin';
import type { OptionItem } from '@/types/api';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';
import { formatDateTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';

const { confirm } = useConfirm();
const toast = useToast();

const sendScopeOptions: OptionItem[] = [
  { label: '全部用户', value: 'all' },
  { label: '指定用户', value: 'single' },
  { label: '指定角色', value: 'role' }
];
const pageSize = 10;

const rows = ref<NotificationRecord[]>([]);
const total = ref(0);
const pageNo = ref(1);
const loading = ref(false);
const error = ref('');

const publishing = ref(false);
const formMessage = ref('');
const form = reactive({
  title: '',
  content: '',
  sendScope: 'all',
  targetUserId: '' as number | string,
  targetRoleId: '' as number | string,
  templateCode: ''
});
const errors = reactive<Record<string, string>>({});

function scopeLabel(scope?: string) {
  return sendScopeOptions.find((o) => o.value === scope)?.label || labelOf(scope);
}

async function reload(nextPage = pageNo.value) {
  pageNo.value = nextPage;
  loading.value = true;
  error.value = '';
  try {
    const result = await pageNotifications({ pageNo: pageNo.value, pageSize });
    rows.value = result.list;
    total.value = result.total;
  } catch (err) {
    error.value = resolveApiError(err, '通知读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.title = '';
  form.content = '';
  form.sendScope = 'all';
  form.targetUserId = '';
  form.targetRoleId = '';
  form.templateCode = '';
  formMessage.value = '';
  Object.keys(errors).forEach((k) => delete errors[k]);
}

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k]);
  formMessage.value = '';
  if (!form.title.trim()) errors.title = '标题不能为空。';
  if (!form.content.trim()) errors.content = '内容不能为空。';
  if (form.sendScope === 'single' && form.targetUserId === '') errors.target = '请选择目标用户。';
  if (form.sendScope === 'role' && form.targetRoleId === '') errors.target = '请选择目标角色。';
  const ok = !Object.keys(errors).length;
  if (!ok) formMessage.value = '请检查标红的字段。';
  return ok;
}

async function publish() {
  if (!validate()) return;
  publishing.value = true;
  const payload: Record<string, unknown> = {
    title: form.title.trim(),
    content: form.content.trim(),
    sendScope: form.sendScope
  };
  if (form.sendScope === 'single' && form.targetUserId !== '') payload.targetUserId = Number(form.targetUserId);
  if (form.sendScope === 'role' && form.targetRoleId !== '') payload.targetRoleId = Number(form.targetRoleId);
  if (form.templateCode.trim()) payload.templateCode = form.templateCode.trim();
  try {
    await createNotification(payload);
    toast.success('通知已发布。');
    resetForm();
    await reload(1);
  } catch (err) {
    formMessage.value = resolveApiError(err, '发布失败。');
  } finally {
    publishing.value = false;
  }
}

async function remove(id: number) {
  const confirmed = await confirm({
    title: '确认删除这条通知？',
    message: '删除后该记录将从列表移除，操作不可撤销。',
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deleteNotification(id);
    toast.success('通知已删除。');
    await reload(pageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '删除失败。'));
  }
}

onMounted(() => reload(1));
</script>

<style scoped>
.notif-page {
  display: grid;
  gap: 22px;
}

.notif-workbench {
  display: grid;
  grid-template-columns: minmax(320px, 380px) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.composer {
  position: sticky;
  top: 16px;
  display: grid;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

.composer-form {
  display: grid;
  gap: 14px;
}

.composer-content :deep(textarea) {
  min-height: 140px;
}

.composer-foot {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  padding-top: 4px;
}

.sent {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.sent-head {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--ink);
}

.sent-head .mono {
  color: var(--muted);
  font-size: var(--font-xs);
}

.notif-list {
  display: grid;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.notif-card {
  display: grid;
  gap: 8px;
  padding: 18px 0;
  border-bottom: 1px solid var(--rule);
}

.notif-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.notif-card__time {
  color: var(--muted);
  font-size: var(--font-xs);
}

.notif-card__title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 19px;
  font-weight: 400;
  line-height: 1.2;
  color: var(--ink);
}

.notif-card__body {
  margin: 0;
  max-width: 78ch;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.notif-card__actions {
  display: flex;
  justify-content: flex-end;
}

.notif-pager--loading {
  opacity: 0.5;
  pointer-events: none;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 980px) {
  .notif-workbench {
    grid-template-columns: 1fr;
  }

  .composer {
    position: static;
  }
}
</style>
