<template>
  <section class="page ops-page">
    <header class="ops-head">
      <div class="section-kicker">§ Ops · Collect</div>
      <h1>采集接入运维</h1>
      <p>手动触发 MinIO 采集批次扫描、重置卡住的采集标记，并查看采集记录处理状态（点行看详情）。</p>
    </header>

    <div class="trigger-card">
      <div class="trigger-row">
        <label>
          <span>采集来源</span>
          <XSelect v-model="sourceCode" :options="sourceOptions" placeholder="全部来源" />
        </label>
        <label>
          <span>单批文件夹数</span>
          <XInput v-model="batchSize" type="number" placeholder="默认按服务端配置" />
        </label>
        <div class="trigger-actions">
          <XButton variant="primary" :loading="scanning" @click="triggerScan">
            <RefreshCcw :size="14" />
            立即扫描
          </XButton>
          <XButton :loading="recovering" @click="triggerRecover">
            <RotateCcw :size="14" />
            重置卡住记录
          </XButton>
        </div>
      </div>
    </div>

    <OpsTaskTable
      ref="tableRef"
      :loader="getCollectRecords"
      :columns="columns"
      :detail-fields="detailFields"
      status-key="collectStatus"
      :status-options="statusOptions"
      empty-title="暂无采集记录"
      empty-description="触发扫描后，采集批次会出现在这里。"
    />
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { RefreshCcw, RotateCcw } from '@lucide/vue';
import XButton from '@/components/common/XButton.vue';
import XInput from '@/components/common/XInput.vue';
import XSelect from '@/components/common/XSelect.vue';
import OpsTaskTable, { type OpsColumn } from './OpsTaskTable.vue';
import { recoverStuckCollectIngest, scanCollectIngest } from '@/api/admin';
import { getCollectRecords } from '@/api/ops';
import { useToast } from '@/composables/useToast';
import { useConfirm } from '@/composables/useConfirm';
import { resolveApiError } from '@/utils/apiError';

const toast = useToast();
const { confirm } = useConfirm();

const sourceOptions = [
  { label: 'FLK 国家法律法规', value: 'flk' },
  { label: 'GB 国家标准', value: 'gb' }
];
const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' }
];

const columns: OpsColumn[] = [
  { key: 'requestUrl', label: '运行文件夹' },
  { key: 'collectStatus', label: '状态', width: '120px', type: 'status' },
  { key: 'httpStatus', label: 'HTTP', width: '80px' },
  { key: 'errorMessage', label: '错误信息' },
  { key: 'startedAt', label: '开始时间', width: '160px', type: 'datetime' },
  { key: 'finishedAt', label: '结束时间', width: '160px', type: 'datetime' }
];

const detailFields: OpsColumn[] = [
  { key: 'id', label: '记录 ID' },
  { key: 'taskId', label: '采集任务 ID' },
  { key: 'sourceId', label: '来源 ID' },
  { key: 'requestUrl', label: '运行文件夹' },
  { key: 'contentHash', label: '内容指纹' },
  { key: 'rawFileId', label: '原始文件 ID' },
  { key: 'httpStatus', label: 'HTTP 状态' },
  { key: 'collectStatus', label: '状态', type: 'status' },
  { key: 'startedAt', label: '开始时间', type: 'datetime' },
  { key: 'finishedAt', label: '结束时间', type: 'datetime' }
];

const sourceCode = ref('');
const batchSize = ref('');
const scanning = ref(false);
const recovering = ref(false);
const tableRef = ref<InstanceType<typeof OpsTaskTable> | null>(null);

async function triggerScan() {
  scanning.value = true;
  try {
    const size = Number(batchSize.value);
    await scanCollectIngest({
      sourceCode: sourceCode.value || undefined,
      batchSize: Number.isFinite(size) && size > 0 ? size : undefined
    });
    toast.success('采集扫描已完成。');
    tableRef.value?.reloadFirst();
  } catch (err) {
    toast.error(resolveApiError(err, '触发采集扫描失败。'));
  } finally {
    scanning.value = false;
  }
}

async function triggerRecover() {
  const ok = await confirm({
    title: '重置卡住的采集记录？',
    message: '将把长时间停留在「处理中」的采集记录重置为「待处理」，下次扫描时重新处理。'
  });
  if (!ok) return;
  recovering.value = true;
  try {
    const count = await recoverStuckCollectIngest();
    toast.success(`已重置 ${count ?? 0} 条卡住记录。`);
    tableRef.value?.reloadFirst();
  } catch (err) {
    toast.error(resolveApiError(err, '重置卡住记录失败。'));
  } finally {
    recovering.value = false;
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

.trigger-card {
  padding: 18px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.trigger-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: end;
}

.trigger-row label {
  display: grid;
  gap: 6px;
  min-width: 200px;
  color: var(--ink-2);
  font-size: 12px;
  font-weight: 600;
}

.trigger-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}
</style>
