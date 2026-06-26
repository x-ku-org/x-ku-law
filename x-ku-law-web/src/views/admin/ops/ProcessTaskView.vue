<template>
  <section class="page ops-page">
    <header class="ops-head">
      <div class="section-kicker">§ Ops · Pipeline</div>
      <h1>法规处理管线</h1>
      <p>查看「文本提取 → 分段 → 发布 → 解读 → 变更分析」处理任务的状态，点任意行查看详情，并对失败任务发起重试。</p>
    </header>

    <OpsTaskTable
      :loader="getProcessTasks"
      :columns="columns"
      :detail-fields="detailFields"
      status-key="processStatus"
      :status-options="statusOptions"
      :retry="retryProcessTask"
      :retry-all="retryAllProcessTasks"
      law-link-key="documentId"
      empty-title="暂无处理任务"
      empty-description="接入法规文件后，文本提取、分段、发布和解读任务会出现在这里。"
    >
      <template #actions="{ reload }">
        <XButton size="small" variant="primary" :loading="triggering" @click="triggerNow(reload)">立即处理一批</XButton>
      </template>
    </OpsTaskTable>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import XButton from '@/components/common/XButton.vue';
import OpsTaskTable, { type OpsColumn } from './OpsTaskTable.vue';
import { getProcessTasks, retryAllProcessTasks, retryProcessTask, triggerProcessTasks } from '@/api/ops';
import { useToast } from '@/composables/useToast';
import { useConfirm } from '@/composables/useConfirm';
import { resolveApiError } from '@/utils/apiError';

const toast = useToast();
const { confirm } = useConfirm();
const triggering = ref(false);

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已完成', value: 'done' },
  { label: '失败', value: 'failed' }
];

const columns: OpsColumn[] = [
  { key: 'documentId', label: '关联法规', type: 'relation', labelField: 'documentTitle' },
  { key: 'processStatus', label: '状态', width: '120px', type: 'status' },
  { key: 'retryCount', label: '重试', width: '72px' },
  { key: 'errorMessage', label: '错误信息' },
  { key: 'startedAt', label: '开始时间', width: '160px', type: 'datetime' },
  { key: 'finishedAt', label: '结束时间', width: '160px', type: 'datetime' }
];

const detailFields: OpsColumn[] = [
  { key: 'id', label: '任务 ID' },
  { key: 'documentId', label: '关联法规', type: 'relation', labelField: 'documentTitle' },
  { key: 'versionId', label: '版本 ID' },
  { key: 'fileId', label: '文件 ID' },
  { key: 'processStatus', label: '状态', type: 'status' },
  { key: 'retryCount', label: '重试次数' },
  { key: 'startedAt', label: '开始时间', type: 'datetime' },
  { key: 'finishedAt', label: '结束时间', type: 'datetime' }
];

async function triggerNow(reload: () => void) {
  const ok = await confirm({
    title: '立即处理一批待处理任务？',
    message: '将从队列取出一批待处理任务立即执行，正常情况下调度会自动处理。'
  });
  if (!ok) return;
  triggering.value = true;
  try {
    const msg = await triggerProcessTasks();
    toast.success(msg || '已触发一批处理任务。');
    reload();
  } catch (err) {
    toast.error(resolveApiError(err, '触发处理失败。'));
  } finally {
    triggering.value = false;
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
</style>
