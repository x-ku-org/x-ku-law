<template>
  <section class="page role-page">
    <AdminPageHeader kicker="§ Roles" title="角色管理" description="维护角色，并为每个角色在权限资源树上勾选授权。" />

    <div class="role-workbench">
      <aside class="role-pane">
        <div class="pane-title">
          <div>
            <strong>角色</strong>
            <span class="mono">共 {{ roles.length }} 个</span>
          </div>
          <XButton size="small" variant="primary" @click="openRoleCreate">新建角色</XButton>
        </div>

        <XInput v-model="roleKeyword" placeholder="搜索名称 / 编码" />

        <PageState v-if="rolesError" :error="rolesError" />
        <SkeletonList v-else-if="rolesLoading" :count="5" />
        <div v-else class="role-list scroll-gutter">
          <button
            v-for="role in filteredRoles"
            :key="role.id"
            type="button"
            class="role-card"
            :class="{ 'role-card--active': role.id === selectedRoleId }"
            @click="selectRole(role.id)"
          >
            <span class="role-card__main">
              <strong>{{ role.roleName || '未命名角色' }}</strong>
              <code>{{ role.roleCode || 'no_code' }}</code>
            </span>
            <StatusBadge :value="role.status || ''" />
          </button>
          <EmptyState
            v-if="!filteredRoles.length"
            title="暂无角色"
            description="先新建一个角色，再为它分配权限。"
          />
        </div>
      </aside>

      <main class="role-detail">
        <EmptyState
          v-if="!selectedRole"
          title="请选择角色"
          description="左侧选择一个角色后，这里显示它的权限授权。"
        />
        <template v-else>
          <header class="detail-head">
            <div>
              <div class="section-kicker">Current Role</div>
              <h2>{{ selectedRole.roleName || selectedRole.roleCode }}</h2>
              <p>
                <code>{{ selectedRole.roleCode }}</code>
                <StatusBadge :value="selectedRole.status || ''" />
              </p>
            </div>
            <div class="detail-actions">
              <XButton variant="ghost" @click="openRoleEdit(selectedRole)">编辑角色</XButton>
              <XButton variant="ghost" @click="removeRole(selectedRole)">删除角色</XButton>
            </div>
          </header>

          <section class="assign-bar">
            <div class="section-kicker">权限分配</div>
            <XInput v-model="permKeyword" class="assign-search" placeholder="过滤权限资源" />
            <div class="assign-bar__tools">
              <button type="button" @click="treeRef?.expandAll()">展开全部</button>
              <button type="button" @click="treeRef?.collapseAll()">收起全部</button>
              <button type="button" @click="selectAll">全选</button>
              <button type="button" @click="clearAll">清空</button>
            </div>
          </section>

          <PageState v-if="permError" :error="permError" />
          <SkeletonTable v-else-if="permLoading" :columns="2" />
          <PermissionTree
            v-else
            ref="treeRef"
            v-model="selectedIds"
            mode="select"
            :nodes="permissions"
            :keyword="permKeyword"
          />

          <div class="save-bar" :class="{ 'save-bar--dirty': dirty }">
            <span class="mono">已选 {{ selectedIds.length }} 项<template v-if="dirty"> · 有未保存改动</template></span>
            <XButton variant="ghost" :disabled="!dirty || saving" @click="resetSelection">还原</XButton>
            <XButton variant="primary" :loading="saving" :disabled="!dirty" @click="savePermissions">保存权限</XButton>
          </div>
        </template>
      </main>
    </div>

    <XModal
      :open="roleModalOpen"
      :title="editingRoleId ? '编辑角色' : '新建角色'"
      description="角色聚合一组权限资源，授权用户后即按此控制访问。"
      kicker="§ Role"
      max-width="640px"
      @update:open="closeRoleModal"
    >
      <form id="role-form" class="modal-form" @submit.prevent="submitRole">
        <div class="field-grid field-grid--two">
          <XFormField label="角色编码" required :error="roleErrors.roleCode">
            <XInput v-model="roleForm.roleCode" placeholder="如 editor" :invalid="Boolean(roleErrors.roleCode)" />
          </XFormField>
          <XFormField label="角色名称" required :error="roleErrors.roleName">
            <XInput v-model="roleForm.roleName" placeholder="如 编辑" :invalid="Boolean(roleErrors.roleName)" />
          </XFormField>
          <XFormField label="排序">
            <XInput v-model="roleForm.sortOrder" type="number" placeholder="数字越小越靠前" />
          </XFormField>
          <XFormField label="状态">
            <XSelect v-model="roleForm.status" :options="enabledOptions" />
          </XFormField>
          <XFormField class="field--wide" label="备注">
            <XTextarea v-model="roleForm.remark" placeholder="补充说明，可留空" />
          </XFormField>
        </div>
      </form>
      <template #footer>
        <span v-if="roleFormMessage" class="form-message">{{ roleFormMessage }}</span>
        <XButton type="button" variant="ghost" @click="closeRoleModal">取消</XButton>
        <XButton variant="primary" type="submit" form="role-form" :loading="roleSubmitting">保存</XButton>
      </template>
    </XModal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import AdminPageHeader from '@/components/business/AdminPageHeader.vue';
import PermissionTree from '@/components/business/PermissionTree.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XSelect from '@/components/common/XSelect.vue';
import XTextarea from '@/components/common/XTextarea.vue';
import {
  assignRolePermissions,
  createRole,
  deleteRole,
  getAllPermissions,
  getRolePermissionIds,
  pageRoles,
  updateRole
} from '@/api/admin';
import type { PermissionRecord, RoleRecord } from '@/types/admin';
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

const treeRef = ref<InstanceType<typeof PermissionTree> | null>(null);

const roles = ref<RoleRecord[]>([]);
const rolesLoading = ref(false);
const rolesError = ref('');
const roleKeyword = ref('');
const selectedRoleId = ref<number | null>(null);

const permissions = ref<PermissionRecord[]>([]);
const permLoading = ref(false);
const permError = ref('');
const permKeyword = ref('');
const selectedIds = ref<number[]>([]);
const savedIds = ref<number[]>([]);
const saving = ref(false);

const filteredRoles = computed(() => {
  const kw = roleKeyword.value.trim().toLowerCase();
  if (!kw) return roles.value;
  return roles.value.filter(
    (r) => (r.roleName || '').toLowerCase().includes(kw) || (r.roleCode || '').toLowerCase().includes(kw)
  );
});
const selectedRole = computed(() => roles.value.find((r) => r.id === selectedRoleId.value) || null);
const dirty = computed(() => {
  if (selectedIds.value.length !== savedIds.value.length) return true;
  const saved = new Set(savedIds.value);
  return selectedIds.value.some((id) => !saved.has(id));
});

async function loadRoles() {
  rolesLoading.value = true;
  rolesError.value = '';
  const previous = selectedRoleId.value;
  try {
    const page = await pageRoles({ pageNo: 1, pageSize: 100 });
    roles.value = page.list;
    const stillThere = page.list.some((r) => r.id === previous);
    const nextId = stillThere ? previous : page.list[0]?.id ?? null;
    if (nextId != null) await selectRole(nextId, true);
    else selectedRoleId.value = null;
  } catch (err) {
    rolesError.value = resolveApiError(err, '角色读取失败。');
    roles.value = [];
  } finally {
    rolesLoading.value = false;
  }
}

async function loadPermissions() {
  if (permissions.value.length) return;
  try {
    permissions.value = await getAllPermissions();
  } catch (err) {
    permError.value = resolveApiError(err, '权限资源读取失败。');
  }
}

async function selectRole(id: number, force = false) {
  if (!force && id === selectedRoleId.value) return;
  selectedRoleId.value = id;
  permKeyword.value = '';
  permLoading.value = true;
  permError.value = '';
  try {
    await loadPermissions();
    const assigned = await getRolePermissionIds(id);
    selectedIds.value = [...assigned];
    savedIds.value = [...assigned];
  } catch (err) {
    permError.value = resolveApiError(err, '角色权限读取失败。');
    selectedIds.value = [];
    savedIds.value = [];
  } finally {
    permLoading.value = false;
  }
}

function selectAll() {
  selectedIds.value = permissions.value.map((p) => p.id);
}
function clearAll() {
  selectedIds.value = [];
}
function resetSelection() {
  selectedIds.value = [...savedIds.value];
}

async function savePermissions() {
  if (selectedRoleId.value == null) return;
  saving.value = true;
  try {
    await assignRolePermissions(selectedRoleId.value, selectedIds.value);
    savedIds.value = [...selectedIds.value];
    toast.success('角色权限已更新。');
  } catch (err) {
    toast.error(resolveApiError(err, '保存权限失败。'));
  } finally {
    saving.value = false;
  }
}

// ===== 角色增改删 =====
const roleModalOpen = ref(false);
const editingRoleId = ref<number | null>(null);
const roleSubmitting = ref(false);
const roleFormMessage = ref('');
const roleForm = reactive({ roleCode: '', roleName: '', sortOrder: '' as number | string, status: 'enabled', remark: '' });
const roleErrors = reactive<Record<string, string>>({});

function resetRoleForm() {
  roleForm.roleCode = '';
  roleForm.roleName = '';
  roleForm.sortOrder = '';
  roleForm.status = 'enabled';
  roleForm.remark = '';
  roleFormMessage.value = '';
  Object.keys(roleErrors).forEach((k) => delete roleErrors[k]);
}

function openRoleCreate() {
  editingRoleId.value = null;
  resetRoleForm();
  roleModalOpen.value = true;
}

function openRoleEdit(role: RoleRecord) {
  editingRoleId.value = role.id;
  resetRoleForm();
  roleForm.roleCode = role.roleCode || '';
  roleForm.roleName = role.roleName || '';
  roleForm.status = role.status || 'enabled';
  roleModalOpen.value = true;
}

function closeRoleModal() {
  roleModalOpen.value = false;
  editingRoleId.value = null;
}

function validateRole() {
  Object.keys(roleErrors).forEach((k) => delete roleErrors[k]);
  roleFormMessage.value = '';
  if (!roleForm.roleCode.trim()) roleErrors.roleCode = '角色编码不能为空。';
  if (!roleForm.roleName.trim()) roleErrors.roleName = '角色名称不能为空。';
  const ok = !Object.keys(roleErrors).length;
  if (!ok) roleFormMessage.value = '请检查标红的字段。';
  return ok;
}

async function submitRole() {
  if (!validateRole()) return;
  roleSubmitting.value = true;
  const isEdit = editingRoleId.value != null;
  const payload: Record<string, unknown> = {
    roleCode: roleForm.roleCode.trim(),
    roleName: roleForm.roleName.trim(),
    status: roleForm.status
  };
  if (roleForm.sortOrder !== '') payload.sortOrder = Number(roleForm.sortOrder);
  if (roleForm.remark.trim()) payload.remark = roleForm.remark.trim();
  try {
    if (isEdit) {
      await updateRole(editingRoleId.value as number, payload);
      toast.success('角色已更新。');
    } else {
      const id = await createRole(payload);
      selectedRoleId.value = id;
      toast.success('角色已创建。');
    }
    closeRoleModal();
    await loadRoles();
  } catch (err) {
    roleFormMessage.value = resolveApiError(err, '保存角色失败。');
  } finally {
    roleSubmitting.value = false;
  }
}

async function removeRole(role: RoleRecord) {
  const confirmed = await confirm({
    title: '确认删除这个角色？',
    message: '删除后该角色的授权将一并移除，已分配此角色的用户会失去相应权限。',
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deleteRole(role.id);
    if (selectedRoleId.value === role.id) selectedRoleId.value = null;
    toast.success('角色已删除。');
    await loadRoles();
  } catch (err) {
    toast.error(resolveApiError(err, '删除角色失败。'));
  }
}

onMounted(loadRoles);
</script>

<style scoped>
.role-page {
  display: grid;
  gap: 22px;
}

.role-workbench {
  display: grid;
  grid-template-columns: minmax(280px, 340px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.role-pane {
  position: sticky;
  top: 16px;
  display: grid;
  gap: 14px;
  padding: 16px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

.pane-title {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.pane-title > div {
  display: grid;
  gap: 4px;
}

.pane-title strong {
  color: var(--ink);
  font-family: var(--serif-body);
  font-size: 18px;
  font-weight: 600;
}

.pane-title .mono {
  color: var(--muted);
  font-size: 12px;
}

.role-list {
  display: grid;
  gap: 8px;
  max-height: calc(100vh - 300px);
  overflow: auto;
}

.role-card {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-card);
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.role-card:hover,
.role-card--active {
  border-color: var(--ink);
  background: var(--paper-2);
}

.role-card__main {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.role-card__main strong {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

code {
  color: var(--ink-3);
  font-family: var(--mono);
  font-size: 12px;
}

.role-detail {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.detail-head {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--rule);
}

.detail-head h2 {
  margin: 6px 0;
  color: var(--ink);
  font-family: var(--serif-display);
  font-size: clamp(24px, 3vw, 32px);
  font-weight: 600;
  line-height: 1.05;
}

.detail-head p {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 0;
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.assign-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.assign-search {
  max-width: 260px;
}

.assign-bar__tools {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.assign-bar__tools button {
  border: 0;
  background: transparent;
  color: var(--accent-deep);
  font-size: 12px;
  cursor: pointer;
}

.assign-bar__tools button:hover {
  color: var(--ink);
}

.save-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

.save-bar--dirty {
  border-color: var(--accent);
}

.save-bar .mono {
  margin-right: auto;
  color: var(--muted);
  font-size: var(--font-xs);
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

@media (max-width: 1080px) {
  .role-workbench {
    grid-template-columns: 1fr;
  }

  .role-pane {
    position: static;
  }
}

@media (max-width: 760px) {
  .field-grid {
    grid-template-columns: 1fr;
  }
}
</style>
