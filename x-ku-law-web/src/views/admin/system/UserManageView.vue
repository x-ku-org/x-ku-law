<template>
  <section class="page user-page">
    <AdminPageHeader kicker="§ Users" title="用户管理" description="维护平台账号、资料与角色授权。点击行查看并编辑详情。">
      <template #actions>
        <XButton variant="primary" @click="openCreate">新建用户</XButton>
      </template>
    </AdminPageHeader>

    <section class="toolbar">
      <XInput v-model="keyword" placeholder="搜索用户名 / 昵称 / 手机号" @keyup.enter="reload(1)" />
      <XSelect v-model="status" :options="statusFilterOptions" placeholder="全部状态" />
      <XButton :loading="loading" @click="reload(1)">筛选</XButton>
    </section>

    <div class="resource-meta">
      <span class="mono">用户</span>
      <span v-if="loading" class="resource-count-sk sk-shimmer" aria-hidden="true" />
      <strong v-else class="mono">共 {{ total }} 人</strong>
      <button v-if="keyword || status" type="button" class="clear-filter" @click="clearFilters">清空筛选</button>
    </div>

    <PageState v-if="error" :error="error" />
    <SkeletonTable v-else-if="loading" :columns="6" show-actions />
    <XTable
      v-else
      dense
      :columns="columns"
      :rows="rows"
      row-clickable
      empty-title="暂无用户"
      :empty-description="keyword || status ? '当前筛选条件下没有用户。' : '还没有用户，点击「新建用户」添加。'"
      @row-click="openDetail"
    >
      <template #cell-identity="{ row }">
        <div class="cell-identity">
          <strong class="mono">{{ row.username || '—' }}</strong>
          <span class="cell-identity__sub">{{ row.realName || row.nickname || '—' }}</span>
        </div>
      </template>
      <template #cell-mobile="{ value }">
        <span class="mono">{{ value || '—' }}</span>
      </template>
      <template #cell-status="{ value }">
        <StatusBadge :value="String(value ?? '')" />
      </template>
      <template #cell-createTime="{ value }">
        <span class="cell-time">{{ formatDateTime(value) }}</span>
      </template>
      <template #actions="{ row }">
        <XButton size="small" variant="ghost" @click.stop="openDetail(row)">详情</XButton>
        <XButton size="small" variant="ghost" @click.stop="remove(row.id as number)">删除</XButton>
      </template>
    </XTable>

    <XPagination
      class="resource-pager"
      :class="{ 'resource-pager--loading': loading }"
      :total="total"
      :page-no="pageNo"
      :page-size="pageSize"
      @change="reload"
    />

    <XModal
      :open="modalOpen"
      :title="editingId ? '用户详情' : '新建用户'"
      :description="editingId ? '编辑账号资料并管理角色授权。' : '填写账号资料以创建用户。'"
      kicker="§ User"
      max-width="600px"
      @update:open="closeModal"
    >
      <form id="user-form" class="modal-form" @submit.prevent="submit">
        <fieldset class="field-group">
          <legend class="field-group__legend">账号资料</legend>
          <div class="field-grid">
            <XFormField label="用户名" required :error="errors.username">
              <XInput v-model="form.username" :disabled="Boolean(editingId)" placeholder="登录用户名" :invalid="Boolean(errors.username)" />
            </XFormField>
            <XFormField label="密码" :hint="editingId ? '留空表示不修改。' : '新建必填。'" :error="errors.password">
              <XInput v-model="form.password" type="password" placeholder="设置登录密码" :invalid="Boolean(errors.password)" />
            </XFormField>
            <XFormField label="真实姓名">
              <XInput v-model="form.realName" placeholder="可选" />
            </XFormField>
            <XFormField label="昵称">
              <XInput v-model="form.nickname" placeholder="可选" />
            </XFormField>
            <XFormField label="手机号">
              <XInput v-model="form.mobile" placeholder="可选" />
            </XFormField>
            <XFormField label="邮箱">
              <XInput v-model="form.email" placeholder="可选" />
            </XFormField>
            <XFormField label="状态">
              <XSelect v-model="form.status" :options="enabledOptions" />
            </XFormField>
          </div>
        </fieldset>

        <fieldset v-if="editingId" class="field-group">
          <legend class="field-group__legend">角色</legend>
          <PageState v-if="rolesError" :error="rolesError" />
          <SkeletonList v-else-if="rolesLoading" :count="3" />
          <template v-else>
            <div v-if="roleChips.length" class="role-chips">
              <XChip v-for="r in roleChips" :key="r.id" tone="accent">{{ r.label }}</XChip>
            </div>
            <p v-else class="role-chips__empty">尚未分配角色。</p>
            <div class="role-picker scroll-gutter">
              <XCheckbox
                v-for="role in allRoles"
                :key="role.id"
                class="role-picker__item"
                :model-value="roleSelected.has(role.id)"
                @update:model-value="toggleRole(role.id)"
              >
                <span class="role-picker__label">{{ role.roleName || role.roleCode || `#${role.id}` }}</span>
                <span v-if="role.roleCode" class="role-picker__sub mono">{{ role.roleCode }}</span>
              </XCheckbox>
              <EmptyState v-if="!allRoles.length" title="暂无可分配角色" description="请先在「角色管理」创建角色。" />
            </div>
          </template>
        </fieldset>
      </form>
      <template #footer>
        <span v-if="formMessage" class="form-message">{{ formMessage }}</span>
        <XButton type="button" variant="ghost" @click="closeModal">取消</XButton>
        <XButton variant="primary" type="submit" form="user-form" :loading="submitting">保存</XButton>
      </template>
    </XModal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import AdminPageHeader from '@/components/business/AdminPageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XCheckbox from '@/components/common/XCheckbox.vue';
import XChip from '@/components/common/XChip.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XPagination from '@/components/common/XPagination.vue';
import XSelect from '@/components/common/XSelect.vue';
import XTable from '@/components/common/XTable.vue';
import {
  assignUserRoles,
  createUser,
  deleteUser,
  getUserRoleIds,
  pageRoles,
  pageUsers,
  updateUser
} from '@/api/admin';
import type { RoleRecord, UserRecord } from '@/types/admin';
import type { OptionItem } from '@/types/api';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';
import { formatDateTime } from '@/utils/datetime';

const { confirm } = useConfirm();
const toast = useToast();

const enabledOptions: OptionItem[] = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];
const statusFilterOptions: OptionItem[] = [{ label: '全部', value: '' }, ...enabledOptions];
const columns = [
  { key: 'identity', label: '用户' },
  { key: 'nickname', label: '昵称' },
  { key: 'mobile', label: '手机号', width: '140px' },
  { key: 'email', label: '邮箱' },
  { key: 'status', label: '状态', width: '110px' },
  { key: 'createTime', label: '创建时间', width: '160px' }
];
const pageSize = 10;

const rows = ref<Record<string, unknown>[]>([]);
const total = ref(0);
const pageNo = ref(1);
const keyword = ref('');
const status = ref('');
const loading = ref(false);
const error = ref('');

const modalOpen = ref(false);
const editingId = ref<number | null>(null);
const submitting = ref(false);
const formMessage = ref('');
const form = reactive({
  username: '',
  password: '',
  realName: '',
  nickname: '',
  mobile: '',
  email: '',
  status: 'enabled'
});
const errors = reactive<Record<string, string>>({});

// 角色分配
const allRoles = ref<RoleRecord[]>([]);
const rolesLoading = ref(false);
const rolesError = ref('');
const roleSelected = ref<Set<number>>(new Set());
const savedRoleIds = ref<number[]>([]);

const roleChips = computed(() =>
  allRoles.value
    .filter((r) => roleSelected.value.has(r.id))
    .map((r) => ({ id: r.id, label: r.roleName || r.roleCode || `#${r.id}` }))
);

async function reload(nextPage = pageNo.value) {
  pageNo.value = nextPage;
  loading.value = true;
  error.value = '';
  try {
    const result = await pageUsers({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value || undefined,
      status: status.value || undefined
    });
    rows.value = result.list as unknown as Record<string, unknown>[];
    total.value = result.total;
  } catch (err) {
    error.value = resolveApiError(err, '用户读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function clearFilters() {
  keyword.value = '';
  status.value = '';
  void reload(1);
}

function resetForm() {
  form.username = '';
  form.password = '';
  form.realName = '';
  form.nickname = '';
  form.mobile = '';
  form.email = '';
  form.status = 'enabled';
  formMessage.value = '';
  Object.keys(errors).forEach((k) => delete errors[k]);
}

async function ensureRoles() {
  if (allRoles.value.length) return;
  rolesLoading.value = true;
  rolesError.value = '';
  try {
    const page = await pageRoles({ pageNo: 1, pageSize: 100 });
    allRoles.value = page.list;
  } catch (err) {
    rolesError.value = resolveApiError(err, '角色读取失败。');
  } finally {
    rolesLoading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  resetForm();
  roleSelected.value = new Set();
  savedRoleIds.value = [];
  modalOpen.value = true;
}

async function openDetail(row: Record<string, unknown>) {
  const user = row as unknown as UserRecord;
  editingId.value = user.id;
  resetForm();
  form.username = user.username || '';
  form.realName = (row.realName as string) || '';
  form.nickname = user.nickname || '';
  form.mobile = user.mobile || '';
  form.email = user.email || '';
  form.status = user.status || 'enabled';
  modalOpen.value = true;
  await ensureRoles();
  rolesLoading.value = true;
  try {
    const ids = await getUserRoleIds(user.id);
    roleSelected.value = new Set(ids);
    savedRoleIds.value = [...ids];
  } catch (err) {
    rolesError.value = resolveApiError(err, '用户角色读取失败。');
  } finally {
    rolesLoading.value = false;
  }
}

function closeModal() {
  modalOpen.value = false;
  editingId.value = null;
}

function toggleRole(id: number) {
  const next = new Set(roleSelected.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  roleSelected.value = next;
}

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k]);
  formMessage.value = '';
  if (!form.username.trim()) errors.username = '用户名不能为空。';
  if (!editingId.value && !form.password.trim()) errors.password = '新建用户必须设置密码。';
  const ok = !Object.keys(errors).length;
  if (!ok) formMessage.value = '请检查标红的字段。';
  return ok;
}

function buildPayload() {
  const payload: Record<string, unknown> = {
    username: form.username.trim(),
    status: form.status
  };
  if (form.password.trim()) payload.password = form.password.trim();
  if (form.realName.trim()) payload.realName = form.realName.trim();
  if (form.nickname.trim()) payload.nickname = form.nickname.trim();
  if (form.mobile.trim()) payload.mobile = form.mobile.trim();
  if (form.email.trim()) payload.email = form.email.trim();
  return payload;
}

function rolesDirty() {
  const saved = new Set(savedRoleIds.value);
  if (saved.size !== roleSelected.value.size) return true;
  return [...roleSelected.value].some((id) => !saved.has(id));
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  const isEdit = editingId.value != null;
  try {
    if (isEdit) {
      const id = editingId.value as number;
      await updateUser(id, buildPayload());
      if (rolesDirty()) await assignUserRoles(id, [...roleSelected.value]);
      toast.success('用户已更新。');
    } else {
      await createUser(buildPayload());
      toast.success('用户已创建。');
    }
    closeModal();
    await reload(isEdit ? pageNo.value : 1);
  } catch (err) {
    formMessage.value = resolveApiError(err, '保存失败。');
  } finally {
    submitting.value = false;
  }
}

async function remove(id: number) {
  const confirmed = await confirm({
    title: '确认删除这个用户？',
    message: '删除后该账号无法登录，操作不可撤销。',
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deleteUser(id);
    if (editingId.value === id) closeModal();
    toast.success('用户已删除。');
    await reload(pageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '删除失败。'));
  }
}

onMounted(() => reload(1));
</script>

<style scoped>
.user-page {
  display: grid;
  gap: 22px;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px auto;
  gap: 12px;
}

.resource-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
}

.resource-meta .mono {
  color: var(--muted);
  font-size: var(--font-xs);
}

.resource-meta strong {
  color: var(--ink);
  font-size: 14px;
}

.resource-count-sk {
  display: inline-block;
  width: 56px;
  height: 10px;
  border-radius: 2px;
}

.clear-filter {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: var(--accent-deep);
  font-size: 12px;
  cursor: pointer;
}

.clear-filter:hover {
  color: var(--ink);
}

.cell-identity {
  display: grid;
  gap: 2px;
}

.cell-identity strong {
  color: var(--ink);
  font-size: 13px;
}

.cell-identity__sub {
  color: var(--muted);
  font-size: var(--font-xs);
}

.cell-time {
  color: var(--ink-3);
  font-variant-numeric: tabular-nums;
}

.resource-pager--loading {
  opacity: 0.5;
  pointer-events: none;
}

.modal-form {
  display: grid;
  gap: 18px;
}

.field-group {
  margin: 0;
  padding: 0;
  border: 0;
}

.field-group + .field-group {
  padding-top: 16px;
  border-top: 1px solid var(--rule);
}

.field-group__legend {
  margin-bottom: 12px;
  padding: 0;
  color: var(--muted);
  font-family: var(--sans);
  font-size: var(--font-xxs);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
}

.role-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.role-chips__empty {
  margin: 0 0 14px;
  color: var(--muted);
  font-size: 13px;
}

.role-picker {
  display: grid;
  gap: 2px;
  max-height: 240px;
  overflow: auto;
}

.role-picker__item {
  display: flex;
  width: 100%;
  min-height: 34px;
  padding: 2px 8px;
  border-radius: 4px;
}

.role-picker__item:hover {
  background: var(--paper-2);
}

.role-picker__item :deep(.x-checkbox__label) {
  display: flex;
  gap: 10px;
  align-items: baseline;
  width: 100%;
  min-width: 0;
}

.role-picker__label {
  color: var(--ink);
  font-size: 13px;
}

.role-picker__sub {
  margin-left: auto;
  color: var(--muted);
  font-size: var(--font-xxs);
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 760px) {
  .toolbar,
  .field-grid {
    grid-template-columns: 1fr;
  }
}
</style>
