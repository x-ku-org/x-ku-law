<template>
  <section class="page ops-page">
    <header class="ops-head">
      <div class="section-kicker">§ Ops · Governance</div>
      <h1>数据治理与审核</h1>
      <p>管线自动发布时记录的数据质量问题与发布审核留痕，以及订阅预警的投递状态；可标记问题已解决、重投失败的预警。</p>
    </header>

    <div class="tabs">
      <button :class="{ active: tab === 'quality' }" type="button" @click="switchTab('quality')">质量问题</button>
      <button :class="{ active: tab === 'audit' }" type="button" @click="switchTab('audit')">审核留痕</button>
      <button :class="{ active: tab === 'alert' }" type="button" @click="switchTab('alert')">预警投递</button>
    </div>

    <OpsTaskTable
      ref="tableRef"
      :loader="loader"
      :columns="columns"
      :detail-fields="detailFields"
      :status-key="statusKey"
      :status-options="statusOptions"
      :retry="retryFn"
      :retry-all="retryAllFn"
      :action="actionFn"
      :action-label="actionLabel"
      action-status="open"
      :empty-title="emptyTitle"
      empty-description="管线运行后，相关记录会出现在这里。"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import OpsTaskTable, { type OpsColumn } from './OpsTaskTable.vue';
import {
  getAlertDeliveries,
  getAuditRecords,
  getQualityIssues,
  resolveQualityIssue,
  retryAlertDelivery,
  retryAllAlertDeliveries
} from '@/api/ops';

type Tab = 'quality' | 'audit' | 'alert';

const tab = ref<Tab>('quality');
const tableRef = ref<InstanceType<typeof OpsTaskTable> | null>(null);

const qualityStatus = [
  { label: '待处理', value: 'open' },
  { label: '已解决', value: 'resolved' }
];
const alertStatus = [
  { label: '待发', value: 'pending' },
  { label: '已送达', value: 'sent' },
  { label: '失败', value: 'failed' }
];

const qualityColumns: OpsColumn[] = [
  { key: 'issueType', label: '问题类型', width: '130px' },
  { key: 'issueLevel', label: '等级', width: '90px' },
  { key: 'refType', label: '对象类型', width: '120px' },
  { key: 'refId', label: '对象 ID', width: '100px' },
  { key: 'issueDesc', label: '描述' },
  { key: 'status', label: '状态', width: '110px', type: 'status' },
  { key: 'createTime', label: '发现时间', width: '160px', type: 'datetime' }
];

const auditColumns: OpsColumn[] = [
  { key: 'auditType', label: '审核类型', width: '140px' },
  { key: 'refType', label: '对象类型', width: '120px' },
  { key: 'refId', label: '对象 ID', width: '100px' },
  { key: 'auditStatus', label: '结论', width: '100px', type: 'status' },
  { key: 'auditComment', label: '审核意见' },
  { key: 'auditTime', label: '审核时间', width: '160px', type: 'datetime' }
];

const alertColumns: OpsColumn[] = [
  { key: 'ruleId', label: '规则 ID', width: '100px' },
  { key: 'matchId', label: '命中 ID', width: '100px' },
  { key: 'userId', label: '用户 ID', width: '100px' },
  { key: 'channel', label: '渠道', width: '90px' },
  { key: 'sendStatus', label: '状态', width: '110px', type: 'status' },
  { key: 'retryCount', label: '重试', width: '72px' },
  { key: 'failReason', label: '失败原因' },
  { key: 'sendTime', label: '送达时间', width: '160px', type: 'datetime' }
];

const loader = computed(() => {
  if (tab.value === 'audit') return getAuditRecords;
  if (tab.value === 'alert') return getAlertDeliveries;
  return getQualityIssues;
});
const columns = computed(() => {
  if (tab.value === 'audit') return auditColumns;
  if (tab.value === 'alert') return alertColumns;
  return qualityColumns;
});
const detailFields = computed(() => columns.value);
const statusKey = computed(() => {
  if (tab.value === 'audit') return 'auditStatus';
  if (tab.value === 'alert') return 'sendStatus';
  return 'status';
});
const statusOptions = computed(() => {
  if (tab.value === 'alert') return alertStatus;
  if (tab.value === 'quality') return qualityStatus;
  return [];
});
const retryFn = computed(() => (tab.value === 'alert' ? retryAlertDelivery : undefined));
const retryAllFn = computed(() => (tab.value === 'alert' ? retryAllAlertDeliveries : undefined));
const actionFn = computed(() => (tab.value === 'quality' ? resolveQualityIssue : undefined));
const actionLabel = computed(() => (tab.value === 'quality' ? '标记解决' : '处理'));
const emptyTitle = computed(() => {
  if (tab.value === 'audit') return '暂无审核留痕';
  if (tab.value === 'alert') return '暂无预警投递';
  return '暂无质量问题';
});

function switchTab(next: Tab) {
  if (tab.value === next) return;
  tab.value = next;
  void nextTick(() => tableRef.value?.reloadFirst());
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
