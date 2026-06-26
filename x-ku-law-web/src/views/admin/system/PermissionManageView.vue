<template>
  <section class="page perm-page">
    <AdminPageHeader kicker="§ Permissions" title="权限资源" description="维护菜单、按钮、接口与数据权限的层级资源树。">
      <template #actions>
        <XButton variant="primary" @click="openCreate(null)">新建顶级资源</XButton>
      </template>
    </AdminPageHeader>

    <section class="perm-toolbar">
      <XInput v-model="keyword" placeholder="搜索名称 / 编码 / 路径" />
      <XSelect v-model="typeFilter" :options="typeFilterOptions" placeholder="全部类型" />
      <div class="perm-toolbar__spacer" />
      <XButton size="small" variant="ghost" @click="treeRef?.expandAll()">展开全部</XButton>
      <XButton size="small" variant="ghost" @click="treeRef?.collapseAll()">收起全部</XButton>
      <XButton size="small" variant="ghost" :loading="loading" @click="reload">刷新</XButton>
    </section>

    <div class="resource-meta">
      <span class="mono">资源总数</span>
      <span v-if="loading" class="resource-count-sk sk-shimmer" aria-hidden="true" />
      <strong v-else class="mono">{{ permissions.length }}</strong>
    </div>

    <PageState v-if="error" :error="error" />
    <SkeletonTable v-else-if="loading" :columns="3" show-actions />
    <PermissionTree
      v-else
      ref="treeRef"
      mode="browse"
      :nodes="permissions"
      :keyword="keyword"
      :type-filter="typeFilter"
      @add-child="openCreate"
      @edit="openEdit"
      @remove="remove"
    />

    <XModal
      :open="modalOpen"
      :title="editingId ? '编辑权限资源' : '新建权限资源'"
      :description="parentLabel ? `归属于：${parentLabel}` : '不选父资源即为顶级资源。'"
      kicker="§ Permission"
      max-width="840px"
      @update:open="closeModal"
    >
      <form id="perm-form" class="modal-form" @submit.prevent="submit">
        <div class="field-grid">
          <XFormField class="field--wide" label="父资源" hint="留空表示顶级资源。">
            <XRelationSelect
              :model-value="form.parentId"
              resource="permissions"
              placeholder="搜索并选择父资源…"
              :initial-label="parentInitialLabel"
              @update:model-value="form.parentId = $event"
            />
          </XFormField>
          <XFormField label="资源编码" required :error="errors.permissionCode">
            <XInput v-model="form.permissionCode" placeholder="如 system:user:create" :invalid="Boolean(errors.permissionCode)" />
          </XFormField>
          <XFormField label="资源名称" required :error="errors.permissionName">
            <XInput v-model="form.permissionName" placeholder="如 新增用户" :invalid="Boolean(errors.permissionName)" />
          </XFormField>
          <XFormField label="资源类型">
            <XSelect v-model="form.permissionType" :options="typeOptions" placeholder="请选择" />
          </XFormField>
          <XFormField label="路径">
            <XInput v-model="form.path" placeholder="如 /admin/users" />
          </XFormField>
          <XFormField label="组件">
            <XInput v-model="form.component" placeholder="如 admin/system/UserManageView" />
          </XFormField>
          <XFormField label="请求方法">
            <XSelect v-model="form.requestMethod" :options="methodOptions" placeholder="不限" />
          </XFormField>
          <XFormField label="排序">
            <XInput v-model="form.sortOrder" type="number" placeholder="数字越小越靠前" />
          </XFormField>
          <XFormField label="状态">
            <XSelect v-model="form.status" :options="enabledOptions" />
          </XFormField>
          <XFormField label="是否可见">
            <XCheckbox v-model="form.visible" label="在菜单中显示" />
          </XFormField>
        </div>
      </form>
      <template #footer>
        <span v-if="formMessage" class="form-message">{{ formMessage }}</span>
        <XButton type="button" variant="ghost" @click="closeModal">取消</XButton>
        <XButton variant="primary" type="submit" form="perm-form" :loading="submitting">保存</XButton>
      </template>
    </XModal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import AdminPageHeader from '@/components/business/AdminPageHeader.vue';
import PermissionTree from '@/components/business/PermissionTree.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import XButton from '@/components/common/XButton.vue';
import XCheckbox from '@/components/common/XCheckbox.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XRelationSelect from '@/components/common/XRelationSelect.vue';
import XSelect from '@/components/common/XSelect.vue';
import { createPermission, deletePermission, getAllPermissions, updatePermission } from '@/api/admin';
import type { PermissionRecord } from '@/types/admin';
import type { OptionItem } from '@/types/api';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';

const { confirm } = useConfirm();
const toast = useToast();

const enabledOptions: OptionItem[] = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];
const typeOptions: OptionItem[] = [
  { label: '菜单', value: 'menu' },
  { label: '按钮', value: 'button' },
  { label: '接口', value: 'api' },
  { label: '数据', value: 'data' }
];
const typeFilterOptions: OptionItem[] = [{ label: '全部类型', value: '' }, ...typeOptions];
const methodOptions: OptionItem[] = [
  { label: '不限', value: '' },
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
];

const treeRef = ref<InstanceType<typeof PermissionTree> | null>(null);
const permissions = ref<PermissionRecord[]>([]);
const loading = ref(false);
const error = ref('');
const keyword = ref('');
const typeFilter = ref('');

const modalOpen = ref(false);
const editingId = ref<number | null>(null);
const submitting = ref(false);
const formMessage = ref('');
const parentLabel = ref('');

const form = reactive({
  parentId: '' as number | string,
  permissionCode: '',
  permissionName: '',
  permissionType: '',
  path: '',
  component: '',
  requestMethod: '',
  sortOrder: '' as number | string,
  status: 'enabled',
  visible: true
});
const errors = reactive<Record<string, string>>({});

const parentInitialLabel = computed(() => parentLabel.value);

async function reload() {
  loading.value = true;
  error.value = '';
  try {
    permissions.value = await getAllPermissions();
  } catch (err) {
    error.value = resolveApiError(err, '权限资源读取失败。');
    permissions.value = [];
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.parentId = '';
  form.permissionCode = '';
  form.permissionName = '';
  form.permissionType = '';
  form.path = '';
  form.component = '';
  form.requestMethod = '';
  form.sortOrder = '';
  form.status = 'enabled';
  form.visible = true;
  formMessage.value = '';
  Object.keys(errors).forEach((k) => delete errors[k]);
}

/** 新建：parent 为 null 即顶级；传入节点即「+子资源」预置父。 */
function openCreate(parent: PermissionRecord | null) {
  editingId.value = null;
  resetForm();
  if (parent) {
    form.parentId = parent.id;
    parentLabel.value = parent.permissionName || `#${parent.id}`;
  } else {
    parentLabel.value = '';
  }
  modalOpen.value = true;
}

function openEdit(node: PermissionRecord) {
  editingId.value = node.id;
  resetForm();
  form.parentId = node.parentId ?? '';
  form.permissionCode = node.permissionCode || '';
  form.permissionName = node.permissionName || '';
  form.permissionType = node.permissionType || '';
  form.path = node.path || '';
  form.component = node.component || '';
  form.requestMethod = node.requestMethod || '';
  form.sortOrder = node.sortOrder ?? '';
  form.status = node.status || 'enabled';
  form.visible = node.visible !== false;
  const parent = node.parentId ? permissions.value.find((p) => p.id === node.parentId) : null;
  parentLabel.value = parent ? parent.permissionName || `#${parent.id}` : '';
  modalOpen.value = true;
}

function closeModal() {
  modalOpen.value = false;
  editingId.value = null;
}

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k]);
  formMessage.value = '';
  if (!form.permissionCode.trim()) errors.permissionCode = '资源编码不能为空。';
  if (!form.permissionName.trim()) errors.permissionName = '资源名称不能为空。';
  const ok = !Object.keys(errors).length;
  if (!ok) formMessage.value = '请检查标红的字段。';
  return ok;
}

function buildPayload() {
  const payload: Record<string, unknown> = {
    permissionCode: form.permissionCode.trim(),
    permissionName: form.permissionName.trim(),
    status: form.status,
    visible: form.visible
  };
  if (form.parentId !== '' && form.parentId !== null) payload.parentId = Number(form.parentId);
  if (form.permissionType) payload.permissionType = form.permissionType;
  if (form.path.trim()) payload.path = form.path.trim();
  if (form.component.trim()) payload.component = form.component.trim();
  if (form.requestMethod) payload.requestMethod = form.requestMethod;
  if (form.sortOrder !== '') payload.sortOrder = Number(form.sortOrder);
  return payload;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  const isEdit = editingId.value != null;
  try {
    if (isEdit) {
      await updatePermission(editingId.value as number, buildPayload());
      toast.success('权限资源已更新。');
    } else {
      await createPermission(buildPayload());
      toast.success('权限资源已创建。');
    }
    closeModal();
    await reload();
  } catch (err) {
    formMessage.value = resolveApiError(err, '保存失败。');
  } finally {
    submitting.value = false;
  }
}

async function remove(node: PermissionRecord) {
  const hasChildren = permissions.value.some((p) => p.parentId === node.id);
  const confirmed = await confirm({
    title: '确认删除这个权限资源？',
    message: hasChildren
      ? '该资源下还有子资源，删除可能影响其子项与已分配的角色。'
      : '删除后使用此资源的角色将失去对应权限，操作不可撤销。',
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deletePermission(node.id);
    toast.success('权限资源已删除。');
    await reload();
  } catch (err) {
    toast.error(resolveApiError(err, '删除失败。'));
  }
}

onMounted(reload);
</script>

<style scoped>
.perm-page {
  display: grid;
  gap: 22px;
}

.perm-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.perm-toolbar :deep(.x-input),
.perm-toolbar :deep(.x-select) {
  max-width: 280px;
}

.perm-toolbar__spacer {
  flex: 1;
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
  width: 48px;
  height: 10px;
  border-radius: 2px;
}

.modal-form {
  display: grid;
  gap: 16px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
}

.field--wide {
  grid-column: 1 / -1;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 760px) {
  .perm-toolbar {
    flex-wrap: wrap;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }
}
</style>
