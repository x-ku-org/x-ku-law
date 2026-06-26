<template>
  <section class="page overview">
    <header class="ov-head">
      <div class="section-kicker">§ Ops · Overview</div>
      <h1>运维总览</h1>
      <p>语料规模、今日更新与各运维管线的健康度一览，可直接下钻到对应模块处理异常。</p>
    </header>

    <section class="ov-stats hairline-strong">
      <div v-for="(s, i) in stats" :key="s.label" class="ov-cell" :class="{ 'has-rule': i < stats.length - 1 }">
        <span class="t-meta-cap">{{ s.kind }}</span>
        <div class="ov-value">
          <Skeleton v-if="loading" variant="stat" width="64px" />
          <strong v-else class="num" :class="{ alert: s.alert && s.value > 0 }">{{ formatNum(s.value) }}</strong>
        </div>
        <span class="ov-label">{{ s.label }}</span>
      </div>
    </section>

    <section class="ov-modules">
      <div class="section-kicker">§ 运维模块 · Modules</div>
      <div class="ov-grid">
        <RouterLink v-for="m in modules" :key="m.title" class="ov-card" :to="m.to">
          <span class="ov-card-icon"><component :is="m.icon" :size="18" /></span>
          <strong class="ov-card-title">{{ m.title }}</strong>
          <small class="ov-card-desc">{{ m.desc }}</small>
          <span v-if="m.badge !== undefined && m.badge > 0" class="ov-card-badge">{{ m.badge }} 待处理</span>
        </RouterLink>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, type RouteLocationRaw } from 'vue-router';
import {
  Bell,
  Clock,
  Database,
  Layers3,
  RefreshCcw,
  ServerCog,
  ShieldCheck,
  UploadCloud,
  Workflow
} from '@lucide/vue';
import Skeleton from '@/components/common/Skeleton.vue';
import { getHomeOverview } from '@/api/portal';
import { getProcessTasks, getIndexTasks, getCollectRecords } from '@/api/ops';

const loading = ref(true);
const corpusCount = ref(0);
const todayUpdateCount = ref(0);
const levelCount = ref(0);
const regionCount = ref(0);
const processFailed = ref(0);
const indexFailed = ref(0);
const collectFailed = ref(0);

const stats = computed(() => [
  { label: '语料总量', kind: 'Corpus', value: corpusCount.value },
  { label: '今日更新', kind: 'Today', value: todayUpdateCount.value },
  { label: '效力层级', kind: 'Levels', value: levelCount.value },
  { label: '覆盖地区', kind: 'Regions', value: regionCount.value },
  { label: '处理失败', kind: 'Pipeline', value: processFailed.value, alert: true },
  { label: '索引失败', kind: 'Index', value: indexFailed.value, alert: true },
  { label: '采集失败', kind: 'Collect', value: collectFailed.value, alert: true }
]);

const modules = computed<{ title: string; desc: string; to: RouteLocationRaw; icon: object; badge?: number }[]>(() => [
  { title: '采集运维', desc: '抓取来源接入与采集记录', to: { name: 'admin.ops.collect' }, icon: RefreshCcw, badge: collectFailed.value },
  { title: '处理管线', desc: '提取 / 分段 / 发布 / 解读', to: { name: 'admin.ops.process' }, icon: Workflow, badge: processFailed.value },
  { title: '索引任务', desc: '检索索引与向量同步', to: { name: 'admin.ops.index' }, icon: ServerCog, badge: indexFailed.value },
  { title: '数据治理', desc: '质量问题与审核留痕', to: { name: 'admin.ops.governance' }, icon: ShieldCheck },
  { title: '定时任务', desc: '调度开关与执行策略', to: { name: 'admin.ops.scheduler' }, icon: Clock },
  { title: '上传接入', desc: '手动上传法规文件入库', to: { name: 'admin.ingest' }, icon: UploadCloud },
  { title: '法规主数据', desc: '法规标题 / 层级 / 时效', to: { name: 'admin.laws' }, icon: Database },
  { title: '法规版本', desc: '版本审核与发布状态', to: { name: 'admin.lawVersions' }, icon: Layers3 },
  { title: '通知管理', desc: '平台通知与发送状态', to: { name: 'admin.notifications' }, icon: Bell }
]);

function formatNum(n: number) {
  return Number(n || 0).toLocaleString('zh-CN');
}

/** 取某条管线「失败」任务的总数；单条任一接口异常不影响其它指标展示。 */
async function failedTotal(loader: (p: { status: string; pageNo: number; pageSize: number }) => Promise<{ total: number }>) {
  try {
    const res = await loader({ status: 'failed', pageNo: 1, pageSize: 1 });
    return res.total || 0;
  } catch {
    return 0;
  }
}

onMounted(async () => {
  const [overview, pf, idf, cf] = await Promise.all([
    getHomeOverview().catch(() => null),
    failedTotal(getProcessTasks),
    failedTotal(getIndexTasks),
    failedTotal(getCollectRecords)
  ]);
  if (overview) {
    corpusCount.value = overview.corpusCount;
    todayUpdateCount.value = overview.todayUpdateCount;
    levelCount.value = overview.levelCount;
    regionCount.value = overview.regionCount;
  }
  processFailed.value = pf;
  indexFailed.value = idf;
  collectFailed.value = cf;
  loading.value = false;
});
</script>

<style scoped>
.overview {
  display: grid;
  gap: 24px;
}

.ov-head {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--rule);
}

.ov-head h1 {
  margin: 6px 0 4px;
  font-family: var(--serif-display);
  font-size: 26px;
  font-weight: 400;
}

.ov-head p {
  margin: 0;
  max-width: 72ch;
  color: var(--ink-3);
  font-size: 13px;
}

.ov-stats {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.ov-cell {
  display: grid;
  gap: 6px;
  padding: 4px 18px;
}

.ov-cell.has-rule {
  border-right: 1px solid var(--rule);
}

.ov-value {
  min-height: 30px;
}

.ov-value .num {
  font-family: var(--serif-display);
  font-size: 26px;
  font-weight: 400;
}

.ov-value .num.alert {
  color: var(--rose);
}

.ov-label {
  color: var(--ink-3);
  font-size: 12px;
}

.ov-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.ov-card {
  position: relative;
  display: grid;
  gap: 4px;
  padding: 16px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
  color: var(--ink-2);
  text-decoration: none;
  transition: border-color 0.14s var(--ease), background 0.14s var(--ease);
}

.ov-card:hover {
  border-color: var(--rule-strong);
  background: var(--paper-card);
}

.ov-card-icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  margin-bottom: 4px;
  border-radius: 4px;
  background: var(--paper-sunk);
  color: var(--ink-2);
}

.ov-card-title {
  font-size: 14px;
  font-weight: 600;
}

.ov-card-desc {
  color: var(--muted);
  font-size: 12px;
}

.ov-card-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 2px 8px;
  border-radius: 3px;
  background: var(--rose-soft);
  color: var(--rose);
  font-size: var(--font-xxs);
  font-weight: 600;
}

@media (max-width: 980px) {
  .ov-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px 0;
  }

  .ov-cell.has-rule:nth-child(even) {
    border-right: 0;
  }
}
</style>
