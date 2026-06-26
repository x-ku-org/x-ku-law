<template>
  <section class="ops">
    <header class="ops-head">
      <div class="section-kicker">§ Ops · Scheduler</div>
      <h1>定时任务配置</h1>
      <p>各调度任务的启用状态、周期与重试。<strong>当前为只读</strong> —— 改动需修改后端 application.yml / 环境变量并重启生效。</p>
    </header>

    <div class="readonly-notice">
      <Lock :size="15" />
      <span>只读视图：调度的启用、周期与重试由后端 <code>application.yml</code> / 环境变量控制，修改后需重启服务方可生效。</span>
    </div>

    <div class="filters">
      <XButton size="small" :loading="loading" @click="load">刷新</XButton>
    </div>

    <PageState v-if="error" :error="error" />
    <SkeletonTable v-if="loading" :columns="columns.length" :show-actions="false" />
    <template v-else>
      <XTable dense :columns="columns" :rows="rows" empty-title="暂无调度配置" empty-description="后端未返回可展示的调度任务配置。">
        <template #cell-enabled="{ value }">
          <StatusBadge :value="value ? '启用' : '停用'" />
        </template>
        <template #cell-configKeys="{ value }">
          <code class="keys">{{ value }}</code>
        </template>
      </XTable>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Lock } from '@lucide/vue';
import XButton from '@/components/common/XButton.vue';
import XTable, { type XTableColumn } from '@/components/common/XTable.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import { getOpsConfig } from '@/api/ops';
import { resolveApiError } from '@/utils/apiError';

const columns: XTableColumn[] = [
  { key: 'name', label: '任务', width: '140px' },
  { key: 'enabled', label: '启用', width: '90px' },
  { key: 'scheduleType', label: '调度', width: '90px' },
  { key: 'schedule', label: '周期 / Cron', width: '170px' },
  { key: 'maxRetry', label: '重试', width: '72px' },
  { key: 'configKeys', label: '配置项 / 环境变量' },
  { key: 'note', label: '说明' }
];

const rows = ref<Record<string, unknown>[]>([]);
const loading = ref(false);
const error = ref('');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    rows.value = (await getOpsConfig()) as unknown as Record<string, unknown>[];
  } catch (err) {
    error.value = resolveApiError(err, '加载定时任务配置失败。');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.ops {
  display: grid;
  gap: 18px;
  padding: 26px;
}

.ops-head h1 {
  margin: 6px 0 4px;
  font-family: var(--serif-display);
  font-size: 26px;
  font-weight: 400;
}

.ops-head p {
  margin: 0;
  color: var(--ink-3);
  font-size: 13px;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.keys {
  font-family: var(--mono);
  font-size: var(--font-xs);
  color: var(--ink-2);
}

.readonly-notice {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 14px;
  border: 1px solid var(--rule-strong);
  border-left: 3px solid var(--gold);
  border-radius: 4px;
  background: var(--gold-soft);
  color: var(--ink-2);
  font-size: 13px;
}

.readonly-notice code {
  font-family: var(--mono);
  font-size: var(--font-xs);
}
</style>
