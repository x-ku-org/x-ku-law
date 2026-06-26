<template>
  <section class="page dict-page">
    <header class="dict-head">
      <div>
        <div class="section-kicker">§ Dictionaries</div>
        <h1 class="h1">字典管理</h1>
        <p>在同一个页面维护字典类型和字典数据。先选择左侧类型，右侧直接编辑该类型下的选项。</p>
      </div>
      <XButton variant="primary" @click="openTypeCreate">新增字典类型</XButton>
    </header>

    <section class="dict-workbench">
      <aside class="type-pane">
        <div class="pane-title">
          <div>
            <strong>字典类型</strong>
            <span class="mono">共 {{ typeTotal }} 类</span>
          </div>
          <XButton size="small" variant="ghost" :loading="typesLoading" @click="reloadTypes()">刷新</XButton>
        </div>

        <div class="type-filters">
          <XInput v-model="typeKeyword" placeholder="搜索编码 / 名称" @keyup.enter="reloadTypes()" />
          <XSelect v-model="typeStatus" :options="filterStatusOptions" placeholder="全部状态" />
          <XButton :loading="typesLoading" @click="reloadTypes()">筛选</XButton>
        </div>

        <PageState v-if="typesError" :error="typesError" />
        <div v-else class="type-list scroll-gutter" :class="{ 'type-list--loading': typesLoading }">
          <button
            v-for="type in dictTypes"
            :key="type.id"
            type="button"
            class="type-card"
            :class="{ 'type-card--active': selectedType?.id === type.id }"
            @click="selectType(type)"
          >
            <span>
              <strong>{{ type.dictName || '未命名字典' }}</strong>
              <code>{{ type.dictCode || 'no_code' }}</code>
            </span>
            <StatusBadge :value="type.status || ''" />
          </button>
          <EmptyState
            v-if="!typesLoading && !dictTypes.length"
            title="暂无字典类型"
            description="先新增一个字典类型，再维护它下面的选项。"
          />
        </div>
      </aside>

      <main class="data-pane">
        <header v-if="selectedType" class="dict-detail-head">
          <div class="dict-detail-head__main">
            <div class="section-kicker">Current Dictionary</div>
            <h2 class="dict-detail-title">{{ selectedType.dictName || selectedType.dictCode }}</h2>
            <div class="dict-detail-meta">
              <code>{{ selectedType.dictCode }}</code>
              <StatusBadge :value="selectedType.status || ''" />
              <span v-if="selectedType.remark" class="dict-remark">{{ selectedType.remark }}</span>
            </div>
          </div>
          <div class="selected-actions">
            <XButton variant="ghost" @click="openTypeEdit(selectedType)">编辑类型</XButton>
            <XButton variant="ghost" @click="removeType(selectedType)">删除类型</XButton>
          </div>
        </header>

        <EmptyState
          v-else
          title="请选择字典类型"
          description="左侧选择一个类型后，这里会显示对应的字典数据。"
        />

        <template v-if="selectedType">
          <form class="quick-add" @submit.prevent="quickAdd">
            <XInput v-model="quick.dictLabel" placeholder="标签（展示文字）" :invalid="Boolean(quickError)" />
            <XInput v-model="quick.dictValue" placeholder="值（保存值）" :invalid="Boolean(quickError)" />
            <XInput v-model="quick.sortOrder" type="number" placeholder="排序" />
            <XButton variant="primary" type="submit" :loading="quickAdding">添加</XButton>
            <XButton type="button" variant="ghost" @click="openDataCreate">更多字段…</XButton>
          </form>
          <p v-if="quickError" class="quick-error">{{ quickError }}</p>

          <section class="data-toolbar">
            <XInput v-model="dataKeyword" placeholder="搜索标签 / 值" @keyup.enter="reloadData(1)" />
            <XSelect v-model="dataStatus" :options="filterStatusOptions" placeholder="全部状态" />
            <XButton :loading="dataLoading" @click="reloadData(1)">筛选</XButton>
          </section>

          <div class="data-meta">
            <span class="mono">当前类型</span>
            <strong>{{ selectedType.dictCode }}</strong>
            <span class="mono">共 {{ dataTotal }} 条</span>
            <button v-if="dataKeyword || dataStatus" type="button" class="clear-filter" @click="clearDataFilters">清空筛选</button>
          </div>

          <PageState v-if="dataError" :error="dataError" />
          <SkeletonTable v-else-if="dataLoading" :columns="dataColumns.length + 1" show-actions />
          <XTable
            v-else
            dense
            :columns="dataColumns"
            :rows="dataRowsAsRecords"
            empty-title="暂无字典数据"
            empty-description="当前字典类型下还没有可用选项。"
          >
            <template #cell-status="{ value }">
              <StatusBadge :value="String(value || '')" />
            </template>
            <template #cell-extJson="{ value }">
              <code v-if="value" class="json-cell">{{ value }}</code>
              <span v-else class="muted">—</span>
            </template>
            <template #actions="{ row }">
              <XButton size="small" variant="ghost" @click="openDataEditFromRow(row)">编辑</XButton>
              <XButton size="small" variant="ghost" @click="removeDataFromRow(row)">删除</XButton>
            </template>
          </XTable>

          <XPagination
            class="data-pager"
            :class="{ 'data-pager--loading': dataLoading }"
            :total="dataTotal"
            :page-no="dataPageNo"
            :page-size="dataPageSize"
            @change="reloadData"
          />
        </template>
      </main>
    </section>

    <XModal
      :open="typeModalOpen"
      :title="editingTypeId ? '编辑字典类型' : '新增字典类型'"
      description="字典类型用于组织一组可复用的下拉、状态或筛选项。"
      kicker="§ Dict Type"
      max-width="720px"
      @update:open="closeTypeModal"
    >
      <form id="dict-type-form" class="modal-form" @submit.prevent="submitType">
        <div class="field-grid field-grid--two">
          <XFormField label="字典编码" required :error="typeErrors.dictCode">
            <XInput v-model="typeForm.dictCode" placeholder="例如 law_type" :invalid="Boolean(typeErrors.dictCode)" />
          </XFormField>
          <XFormField label="字典名称" required :error="typeErrors.dictName">
            <XInput v-model="typeForm.dictName" placeholder="例如 法规类型" :invalid="Boolean(typeErrors.dictName)" />
          </XFormField>
          <XFormField label="状态">
            <XSelect v-model="typeForm.status" :options="statusOptions" />
          </XFormField>
          <XFormField class="field--wide" label="备注">
            <XTextarea v-model="typeForm.remark" placeholder="补充说明，可留空" />
          </XFormField>
        </div>
      </form>
      <template #footer>
        <span v-if="typeFormMessage" class="form-message">{{ typeFormMessage }}</span>
        <XButton type="button" variant="ghost" @click="closeTypeModal">取消</XButton>
        <XButton variant="primary" type="submit" form="dict-type-form" :loading="typeSubmitting">保存</XButton>
      </template>
    </XModal>

    <XModal
      :open="dataModalOpen"
      :title="editingDataId ? '编辑字典数据' : '新增字典数据'"
      :description="selectedType ? `所属字典：${selectedType.dictName || selectedType.dictCode}` : '维护当前字典类型下的数据项。'"
      kicker="§ Dict Data"
      max-width="860px"
      @update:open="closeDataModal"
    >
      <form id="dict-data-form" class="modal-form" @submit.prevent="submitData">
        <div class="locked-dict" v-if="selectedType">
          <span class="mono">所属类型</span>
          <strong>{{ selectedType.dictName || selectedType.dictCode }}</strong>
          <code>{{ selectedType.dictCode }}</code>
        </div>
        <div class="field-grid">
          <XFormField label="标签" required :error="dataErrors.dictLabel">
            <XInput v-model="dataForm.dictLabel" placeholder="展示给用户看的文字" :invalid="Boolean(dataErrors.dictLabel)" />
          </XFormField>
          <XFormField label="值" required :error="dataErrors.dictValue">
            <XInput v-model="dataForm.dictValue" placeholder="系统保存的值" :invalid="Boolean(dataErrors.dictValue)" />
          </XFormField>
          <XFormField label="父级值">
            <XInput v-model="dataForm.parentValue" placeholder="可选" />
          </XFormField>
          <XFormField label="排序">
            <XInput v-model="dataForm.sortOrder" type="number" placeholder="数字越小越靠前" />
          </XFormField>
          <XFormField label="状态">
            <XSelect v-model="dataForm.status" :options="statusOptions" />
          </XFormField>
          <XFormField class="field--wide" label="扩展 JSON" :error="dataErrors.extJson" hint="高级属性，必须是合法 JSON；留空表示无。">
            <XTextarea v-model="dataForm.extJson" class="json-textarea" placeholder='例如 {"color":"blue"}' />
          </XFormField>
        </div>
      </form>
      <template #footer>
        <span v-if="dataFormMessage" class="form-message">{{ dataFormMessage }}</span>
        <XButton type="button" variant="ghost" @click="closeDataModal">取消</XButton>
        <XButton variant="primary" type="submit" form="dict-data-form" :loading="dataSubmitting">保存</XButton>
      </template>
    </XModal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  pageDictData,
  pageDictTypes,
  updateDictData,
  updateDictType
} from '@/api/admin';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XPagination from '@/components/common/XPagination.vue';
import XSelect from '@/components/common/XSelect.vue';
import XTable from '@/components/common/XTable.vue';
import XTextarea from '@/components/common/XTextarea.vue';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';
import type { OptionItem } from '@/types/api';
import type { DictDataRecord, DictTypeRecord } from '@/types/admin';
import { resolveApiError } from '@/utils/apiError';

const statusOptions: OptionItem[] = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];
const filterStatusOptions: OptionItem[] = [{ label: '全部状态', value: '' }, ...statusOptions];
const dataColumns = [
  { key: 'dictLabel', label: '标签' },
  { key: 'dictValue', label: '值' },
  { key: 'parentValue', label: '父级值', width: '120px' },
  { key: 'sortOrder', label: '排序', width: '80px' },
  { key: 'status', label: '状态', width: '120px' },
  { key: 'extJson', label: '扩展', width: '180px' }
];
const dataPageSize = 10;

const { confirm } = useConfirm();
const toast = useToast();

const dictTypes = ref<DictTypeRecord[]>([]);
const selectedTypeId = ref<number | null>(null);
const typeTotal = ref(0);
const typeKeyword = ref('');
const typeStatus = ref('');
const typesLoading = ref(false);
const typesError = ref('');

const dataRows = ref<DictDataRecord[]>([]);
const dataTotal = ref(0);
const dataPageNo = ref(1);
const dataKeyword = ref('');
const dataStatus = ref('');
const dataLoading = ref(false);
const dataError = ref('');

const typeModalOpen = ref(false);
const editingTypeId = ref<number | null>(null);
const typeSubmitting = ref(false);
const typeFormMessage = ref('');
const typeForm = reactive({
  dictCode: '',
  dictName: '',
  status: 'enabled',
  remark: ''
});
const typeErrors = reactive<Record<string, string>>({});

const dataModalOpen = ref(false);
const editingDataId = ref<number | null>(null);
const dataSubmitting = ref(false);
const dataFormMessage = ref('');
const dataForm = reactive({
  dictLabel: '',
  dictValue: '',
  parentValue: '',
  sortOrder: '',
  status: 'enabled',
  extJson: ''
});
const dataErrors = reactive<Record<string, string>>({});

// 行内快捷新增：覆盖「标签/值/排序」高频录入，完整字段仍走弹窗。
const quick = reactive({ dictLabel: '', dictValue: '', sortOrder: '' });
const quickAdding = ref(false);
const quickError = ref('');

const selectedType = computed(() => dictTypes.value.find((type) => type.id === selectedTypeId.value) || null);
const dataRowsAsRecords = computed<Record<string, unknown>[]>(() => dataRows.value as unknown as Record<string, unknown>[]);

function clearErrors(errors: Record<string, string>) {
  Object.keys(errors).forEach((key) => {
    delete errors[key];
  });
}

async function reloadTypes() {
  typesLoading.value = true;
  typesError.value = '';
  const previousId = selectedTypeId.value;
  try {
    const result = await pageDictTypes({
      pageNo: 1,
      pageSize: 100,
      keyword: typeKeyword.value || undefined,
      status: typeStatus.value || undefined
    });
    dictTypes.value = result.list;
    typeTotal.value = result.total;
    selectedTypeId.value = result.list.some((type) => type.id === previousId) ? previousId : result.list[0]?.id ?? null;
    await reloadData(1);
  } catch (err) {
    typesError.value = resolveApiError(err, '字典类型读取失败。');
    dictTypes.value = [];
    selectedTypeId.value = null;
    dataRows.value = [];
    dataTotal.value = 0;
  } finally {
    typesLoading.value = false;
  }
}

function selectType(type: DictTypeRecord) {
  if (selectedTypeId.value === type.id) return;
  selectedTypeId.value = type.id;
  void reloadData(1);
}

async function reloadData(nextPage = dataPageNo.value) {
  dataPageNo.value = nextPage;
  dataError.value = '';
  if (!selectedType.value?.dictCode) {
    dataRows.value = [];
    dataTotal.value = 0;
    return;
  }
  dataLoading.value = true;
  try {
    const result = await pageDictData({
      pageNo: dataPageNo.value,
      pageSize: dataPageSize,
      dictCode: selectedType.value.dictCode,
      keyword: dataKeyword.value || undefined,
      status: dataStatus.value || undefined
    });
    dataRows.value = result.list;
    dataTotal.value = result.total;
  } catch (err) {
    dataError.value = resolveApiError(err, '字典数据读取失败。');
    dataRows.value = [];
    dataTotal.value = 0;
  } finally {
    dataLoading.value = false;
  }
}

function clearDataFilters() {
  dataKeyword.value = '';
  dataStatus.value = '';
  void reloadData(1);
}

function resetTypeForm() {
  typeForm.dictCode = '';
  typeForm.dictName = '';
  typeForm.status = 'enabled';
  typeForm.remark = '';
  typeFormMessage.value = '';
  clearErrors(typeErrors);
}

function openTypeCreate() {
  editingTypeId.value = null;
  resetTypeForm();
  typeModalOpen.value = true;
}

function openTypeEdit(type: DictTypeRecord) {
  editingTypeId.value = type.id;
  typeForm.dictCode = type.dictCode || '';
  typeForm.dictName = type.dictName || '';
  typeForm.status = type.status || 'enabled';
  typeForm.remark = type.remark || '';
  typeFormMessage.value = '';
  clearErrors(typeErrors);
  typeModalOpen.value = true;
}

function closeTypeModal() {
  typeModalOpen.value = false;
  editingTypeId.value = null;
  resetTypeForm();
}

function validateTypeForm() {
  clearErrors(typeErrors);
  typeFormMessage.value = '';
  if (!typeForm.dictCode.trim()) typeErrors.dictCode = '字典编码不能为空。';
  if (!typeForm.dictName.trim()) typeErrors.dictName = '字典名称不能为空。';
  const valid = !Object.keys(typeErrors).length;
  if (!valid) typeFormMessage.value = '请检查标红的字段。';
  return valid;
}

async function submitType() {
  if (!validateTypeForm()) return;
  typeSubmitting.value = true;
  const isEdit = editingTypeId.value != null;
  const payload = {
    dictCode: typeForm.dictCode.trim(),
    dictName: typeForm.dictName.trim(),
    status: typeForm.status,
    remark: typeForm.remark.trim() || undefined
  };
  try {
    if (isEdit) {
      await updateDictType(editingTypeId.value as number, payload);
      toast.success('字典类型已更新。');
    } else {
      const id = await createDictType(payload);
      selectedTypeId.value = id;
      toast.success('字典类型已创建。');
    }
    closeTypeModal();
    await reloadTypes();
  } catch (err) {
    typeFormMessage.value = resolveApiError(err, '保存字典类型失败。');
  } finally {
    typeSubmitting.value = false;
  }
}

async function removeType(type: DictTypeRecord) {
  const confirmed = await confirm({
    title: '确认删除这个字典类型？',
    message: '删除类型可能会影响它下面的字典数据和使用这些字典的页面。',
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deleteDictType(type.id);
    toast.success('字典类型已删除。');
    await reloadTypes();
  } catch (err) {
    toast.error(resolveApiError(err, '删除字典类型失败。'));
  }
}

function resetDataForm() {
  dataForm.dictLabel = '';
  dataForm.dictValue = '';
  dataForm.parentValue = '';
  dataForm.sortOrder = '';
  dataForm.status = 'enabled';
  dataForm.extJson = '';
  dataFormMessage.value = '';
  clearErrors(dataErrors);
}

function openDataCreate() {
  editingDataId.value = null;
  resetDataForm();
  dataModalOpen.value = true;
}

function openDataEdit(row: DictDataRecord) {
  editingDataId.value = row.id;
  dataForm.dictLabel = row.dictLabel || '';
  dataForm.dictValue = row.dictValue || '';
  dataForm.parentValue = row.parentValue || '';
  dataForm.sortOrder = row.sortOrder === undefined || row.sortOrder === null ? '' : String(row.sortOrder);
  dataForm.status = row.status || 'enabled';
  dataForm.extJson = row.extJson || '';
  dataFormMessage.value = '';
  clearErrors(dataErrors);
  dataModalOpen.value = true;
}

function openDataEditFromRow(row: Record<string, unknown>) {
  openDataEdit(row as unknown as DictDataRecord);
}

function closeDataModal() {
  dataModalOpen.value = false;
  editingDataId.value = null;
  resetDataForm();
}

function validateDataForm() {
  clearErrors(dataErrors);
  dataFormMessage.value = '';
  if (!dataForm.dictLabel.trim()) dataErrors.dictLabel = '标签不能为空。';
  if (!dataForm.dictValue.trim()) dataErrors.dictValue = '值不能为空。';
  if (dataForm.extJson.trim()) {
    try {
      JSON.parse(dataForm.extJson);
    } catch {
      dataErrors.extJson = 'JSON 格式不正确。';
    }
  }
  const valid = !Object.keys(dataErrors).length;
  if (!valid) dataFormMessage.value = '请检查标红的字段。';
  return valid;
}

function buildDataPayload() {
  const type = selectedType.value;
  const payload: Record<string, unknown> = {
    dictTypeId: type?.id,
    dictCode: type?.dictCode,
    dictLabel: dataForm.dictLabel.trim(),
    dictValue: dataForm.dictValue.trim(),
    status: dataForm.status
  };
  if (dataForm.parentValue.trim()) payload.parentValue = dataForm.parentValue.trim();
  if (dataForm.sortOrder !== '') payload.sortOrder = Number(dataForm.sortOrder);
  if (dataForm.extJson.trim()) payload.extJson = dataForm.extJson.trim();
  return payload;
}

async function submitData() {
  if (!selectedType.value || !validateDataForm()) return;
  dataSubmitting.value = true;
  const isEdit = editingDataId.value != null;
  try {
    if (isEdit) {
      await updateDictData(editingDataId.value as number, buildDataPayload());
      toast.success('字典数据已更新。');
    } else {
      await createDictData(buildDataPayload());
      toast.success('字典数据已创建。');
    }
    closeDataModal();
    await reloadData(isEdit ? dataPageNo.value : 1);
  } catch (err) {
    dataFormMessage.value = resolveApiError(err, '保存字典数据失败。');
  } finally {
    dataSubmitting.value = false;
  }
}

async function quickAdd() {
  quickError.value = '';
  if (!selectedType.value) return;
  if (!quick.dictLabel.trim() || !quick.dictValue.trim()) {
    quickError.value = '标签和值都不能为空。';
    return;
  }
  quickAdding.value = true;
  const payload: Record<string, unknown> = {
    dictTypeId: selectedType.value.id,
    dictCode: selectedType.value.dictCode,
    dictLabel: quick.dictLabel.trim(),
    dictValue: quick.dictValue.trim(),
    status: 'enabled'
  };
  if (quick.sortOrder !== '') payload.sortOrder = Number(quick.sortOrder);
  try {
    await createDictData(payload);
    quick.dictLabel = '';
    quick.dictValue = '';
    quick.sortOrder = '';
    toast.success('字典数据已添加。');
    await reloadData(1);
  } catch (err) {
    quickError.value = resolveApiError(err, '添加失败。');
  } finally {
    quickAdding.value = false;
  }
}

async function removeData(row: DictDataRecord) {
  const confirmed = await confirm({
    title: '确认删除这条字典数据？',
    message: `将删除「${row.dictLabel || row.dictValue || row.id}」，此操作不可撤销。`,
    confirmText: '确认删除',
    danger: true
  });
  if (!confirmed) return;
  try {
    await deleteDictData(row.id);
    toast.success('字典数据已删除。');
    await reloadData(dataPageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '删除字典数据失败。'));
  }
}

function removeDataFromRow(row: Record<string, unknown>) {
  void removeData(row as unknown as DictDataRecord);
}

onMounted(() => {
  void reloadTypes();
});
</script>

<style scoped>
.dict-page {
  display: grid;
  gap: 22px;
}

.dict-head {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--ink);
}

.dict-head p {
  max-width: 72ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.65;
}

.dict-workbench {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.type-pane,
.data-pane {
  min-width: 0;
}

.type-pane {
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

.type-filters,
.data-toolbar {
  display: grid;
  gap: 10px;
  align-items: stretch;
}

.type-filters {
  grid-template-columns: 1fr;
}

.data-toolbar {
  grid-template-columns: minmax(220px, 1fr) 180px auto;
}

.quick-add {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) minmax(160px, 1fr) 100px auto auto;
  gap: 10px;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.quick-error {
  margin: -6px 0 0;
  color: var(--rose);
  font-size: 12px;
}

.type-list {
  display: grid;
  gap: 8px;
  max-height: calc(100vh - 320px);
  min-height: 220px;
  overflow: auto;
}

.type-list--loading {
  opacity: 0.55;
  pointer-events: none;
}

.type-card {
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
  transition:
    border-color 0.15s var(--ease),
    background 0.15s var(--ease);
}

.type-card:hover,
.type-card--active {
  border-color: var(--ink);
  background: var(--paper-2);
}

.type-card > span {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.type-card strong {
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

.data-pane {
  display: grid;
  gap: 16px;
}

.dict-detail-head {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--ink);
}

.dict-detail-head__main {
  min-width: 0;
}

.dict-detail-title {
  margin: 6px 0 8px;
  color: var(--ink);
  font-family: var(--serif-display);
  font-size: clamp(22px, 2.6vw, 28px);
  font-weight: 600;
  line-height: 1.1;
}

.dict-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.dict-remark {
  color: var(--ink-3);
  font-size: 13px;
}

.selected-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.data-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
}

.data-meta .mono,
.muted {
  color: var(--muted);
  font-size: 12px;
}

.data-meta strong {
  color: var(--ink);
  font-family: var(--serif-body);
  font-size: 14px;
  font-weight: 500;
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

.json-cell {
  display: inline-block;
  max-width: 18ch;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: top;
  white-space: nowrap;
}

.data-pager--loading {
  opacity: 0.5;
  pointer-events: none;
}

.modal-form {
  display: grid;
  gap: 16px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 20px;
}

.field-grid--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field--wide {
  grid-column: 1 / -1;
}

.locked-dict {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.locked-dict .mono {
  color: var(--muted);
  font-size: 12px;
}

.locked-dict strong {
  color: var(--ink);
  font-size: 13px;
}

.json-textarea :deep(textarea) {
  font-family: var(--mono);
  font-size: 12px;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 1080px) {
  .dict-workbench {
    grid-template-columns: 1fr;
  }

  .type-pane {
    position: static;
  }

  .type-list {
    max-height: 360px;
  }
}

@media (max-width: 760px) {
  .dict-head,
  .dict-detail-head {
    display: grid;
  }

  .data-toolbar,
  .quick-add,
  .field-grid,
  .field-grid--two {
    grid-template-columns: 1fr;
  }
}
</style>
