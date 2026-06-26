<template>
  <section class="page ops-page">
    <header class="ops-head">
      <div class="section-kicker">§ Ops · Index</div>
      <h1>索引与向量任务</h1>
      <p>查看检索索引同步与向量同步任务的状态，点任意行查看详情，并对失败任务发起重试。</p>
    </header>

    <div class="tabs">
      <button :class="{ active: tab === 'index' }" type="button" @click="switchTab('index')">检索索引</button>
      <button :class="{ active: tab === 'vector' }" type="button" @click="switchTab('vector')">向量同步</button>
    </div>

    <OpsTaskTable
      ref="tableRef"
      :loader="loader"
      :columns="columns"
      :detail-fields="detailFields"
      status-key="syncStatus"
      :status-options="statusOptions"
      :retry="retryFn"
      :retry-all="retryAllFn"
      empty-title="暂无同步任务"
      empty-description="触发同步或回填后，索引与向量任务会出现在这里。"
    >
      <template #actions="{ reload }">
        <XButton size="small" variant="primary" :loading="triggering" @click="triggerNow(reload)">立即同步一批</XButton>
        <XButton
          v-if="tab === 'index'"
          size="small"
          :loading="backfilling"
          title="为已发布且为当前版本的法规批量入队 upsert，用于新增索引字段后回填全库"
          @click="backfill(reload)"
        >
          回填全库
        </XButton>
      </template>
    </OpsTaskTable>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import XButton from '@/components/common/XButton.vue';
import OpsTaskTable, { type OpsColumn } from './OpsTaskTable.vue';
import {
  backfillIndexTasks,
  getIndexTasks,
  getVectorTasks,
  retryAllIndexTasks,
  retryAllVectorTasks,
  retryIndexTask,
  retryVectorTask,
  triggerIndexTasks,
  triggerVectorTasks
} from '@/api/ops';
import { useToast } from '@/composables/useToast';
import { useConfirm } from '@/composables/useConfirm';
import { resolveApiError } from '@/utils/apiError';

type Tab = 'index' | 'vector';

const toast = useToast();
const { confirm } = useConfirm();
const tab = ref<Tab>('index');
const tableRef = ref<InstanceType<typeof OpsTaskTable> | null>(null);
const triggering = ref(false);
const backfilling = ref(false);

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已完成', value: 'done' },
  { label: '失败', value: 'failed' }
];

const indexColumns: OpsColumn[] = [
  { key: 'refType', label: '对象类型', width: '120px' },
  { key: 'refId', label: '对象 ID', width: '110px' },
  { key: 'actionType', label: '动作', width: '96px' },
  { key: 'syncStatus', label: '状态', width: '120px', type: 'status' },
  { key: 'retryCount', label: '重试', width: '72px' },
  { key: 'errorMessage', label: '错误信息' },
  { key: 'lastSyncTime', label: '最近同步', width: '160px', type: 'datetime' }
];

const vectorColumns: OpsColumn[] = [
  { key: 'refType', label: '对象类型', width: '120px' },
  { key: 'refId', label: '对象 ID', width: '110px' },
  { key: 'vectorIndex', label: '向量索引', width: '140px' },
  { key: 'syncStatus', label: '状态', width: '120px', type: 'status' },
  { key: 'retryCount', label: '重试', width: '72px' },
  { key: 'errorMessage', label: '错误信息' },
  { key: 'lastSyncTime', label: '最近同步', width: '160px', type: 'datetime' }
];

const indexDetail: OpsColumn[] = [
  { key: 'id', label: '任务 ID' },
  { key: 'refType', label: '对象类型' },
  { key: 'refId', label: '对象 ID' },
  { key: 'indexName', label: '索引名' },
  { key: 'actionType', label: '动作' },
  { key: 'syncStatus', label: '状态', type: 'status' },
  { key: 'retryCount', label: '重试次数' },
  { key: 'lastSyncTime', label: '最近同步', type: 'datetime' }
];

const vectorDetail: OpsColumn[] = [
  { key: 'id', label: '任务 ID' },
  { key: 'refType', label: '对象类型' },
  { key: 'refId', label: '对象 ID' },
  { key: 'vectorIndex', label: '向量索引' },
  { key: 'vectorId', label: '向量 ID' },
  { key: 'actionType', label: '动作' },
  { key: 'syncStatus', label: '状态', type: 'status' },
  { key: 'retryCount', label: '重试次数' },
  { key: 'lastSyncTime', label: '最近同步', type: 'datetime' }
];

const loader = computed(() => (tab.value === 'index' ? getIndexTasks : getVectorTasks));
const retryFn = computed(() => (tab.value === 'index' ? retryIndexTask : retryVectorTask));
const retryAllFn = computed(() => (tab.value === 'index' ? retryAllIndexTasks : retryAllVectorTasks));
const columns = computed(() => (tab.value === 'index' ? indexColumns : vectorColumns));
const detailFields = computed(() => (tab.value === 'index' ? indexDetail : vectorDetail));

function switchTab(next: Tab) {
  if (tab.value === next) return;
  tab.value = next;
  void nextTick(() => tableRef.value?.reloadFirst());
}

async function triggerNow(reload: () => void) {
  const ok = await confirm({
    title: tab.value === 'index' ? '立即同步一批索引任务？' : '立即同步一批向量任务？',
    message: '将从队列取出一批待同步任务立即执行。'
  });
  if (!ok) return;
  triggering.value = true;
  try {
    const msg = tab.value === 'index' ? await triggerIndexTasks() : await triggerVectorTasks();
    toast.success(msg || '已触发一批同步任务。');
    reload();
  } catch (err) {
    toast.error(resolveApiError(err, '触发同步失败。'));
  } finally {
    triggering.value = false;
  }
}

async function backfill(reload: () => void) {
  const ok = await confirm({
    title: '回填全库索引？',
    message: '将为全部「已发布且为当前版本」的法规批量入队 upsert 索引任务，用于新增索引字段后回填。任务量可能较大，由同步调度逐批处理。',
    confirmText: '开始回填'
  });
  if (!ok) return;
  backfilling.value = true;
  try {
    const enqueued = await backfillIndexTasks();
    toast.success(enqueued > 0 ? `已入队 ${enqueued} 条回填任务，将由同步调度逐批处理。` : '没有需要回填的法规（均已在途或已入队）。');
    reload();
  } catch (err) {
    toast.error(resolveApiError(err, '回填失败。'));
  } finally {
    backfilling.value = false;
  }
}
</script>

<style scoped>
.ops-page {
  display: grid;
  gap: 18px;
}

.ops-head {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--rule);
}

.ops-head h1 {
  margin: 6px 0 4px;
  font-family: var(--serif-display);
  font-size: 26px;
  font-weight: 400;
}

.ops-head p {
  margin: 0;
  max-width: 72ch;
  color: var(--ink-3);
  font-size: 13px;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tabs button {
  height: var(--control-h-sm);
  padding: 0 16px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink-2);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.tabs button.active {
  border-color: var(--ink);
  background: var(--ink);
  color: var(--paper);
}
</style>
