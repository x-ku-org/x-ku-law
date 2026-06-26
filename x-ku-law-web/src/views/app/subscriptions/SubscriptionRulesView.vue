<template>
  <section class="page rules">
    <header class="rules-head">
      <div>
        <div class="section-kicker">§ Subscription</div>
        <h1 class="h1">订阅规则</h1>
        <p>按关键词与条件维护法规变更提醒，命中后即时推送到预警中心。</p>
      </div>
      <XButton variant="primary" @click="openCreate">新建规则</XButton>
    </header>

    <div class="rules-toolbar">
      <XSelect v-model="status" :options="statusFilterOptions" placeholder="全部状态" />
      <XButton :loading="loading" @click="reload(1)">筛选</XButton>
      <span class="rules-count mono">共 {{ total }} 条</span>
    </div>

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" :count="4" />
    <EmptyState
      v-else-if="!rows.length"
      title="还没有订阅规则"
      description="创建规则后，命中的法规变更会自动推送到预警中心。"
    >
      <XButton variant="primary" @click="openCreate">新建第一条订阅规则</XButton>
    </EmptyState>

    <div v-else class="rule-grid">
      <article v-for="rule in rows" :key="rule.id" class="rule-card" :class="{ off: rule.status !== 'enabled' }">
        <div class="rule-card__top">
          <h3 class="rule-card__name">{{ rule.ruleName || `规则 #${rule.id}` }}</h3>
          <StatusBadge :value="rule.status" />
        </div>

        <div v-if="keywordsOf(rule).length" class="rule-card__chips">
          <span class="rule-card__tag mono">关键词</span>
          <XChip v-for="kw in keywordsOf(rule)" :key="kw" tone="accent">{{ kw }}</XChip>
        </div>
        <div v-else class="rule-card__muted">未设关键词，命中取决于下方条件</div>

        <div class="rule-card__chips">
          <span class="rule-card__tag mono">条件</span>
          <template v-if="conditionChips(rule).length">
            <XChip v-for="c in conditionChips(rule)" :key="c" tone="outline">{{ c }}</XChip>
          </template>
          <span v-else class="rule-card__muted">不限</span>
        </div>

        <div class="rule-card__foot">
          <span class="mono">{{ channelLabel(rule.deliveryChannel) }}</span>
          <span class="mono dot">·</span>
          <span class="mono">{{ rule.lastRunTime ? `上次运行 ${formatDateTime(rule.lastRunTime)}` : '尚未运行' }}</span>
        </div>

        <div class="rule-card__actions">
          <XButton
            size="small"
            :loading="togglingId === rule.id"
            @click="toggle(rule)"
          >{{ rule.status === 'enabled' ? '停用' : '启用' }}</XButton>
          <XButton size="small" variant="ghost" @click="goMatches(rule)">命中记录 →</XButton>
          <span class="rule-card__spacer" />
          <XButton size="small" variant="ghost" @click="openEdit(rule)">编辑</XButton>
          <XButton size="small" variant="ghost" @click="remove(rule)">删除</XButton>
        </div>
      </article>
    </div>

    <XPagination
      v-if="rows.length"
      class="rules-pager"
      :total="total"
      :page-no="pageNo"
      :page-size="pageSize"
      @change="reload"
    />

    <SubscriptionRuleForm :open="formOpen" :rule="editing" @update:open="formOpen = $event" @saved="onSaved" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import XPagination from '@/components/common/XPagination.vue';
import XSelect from '@/components/common/XSelect.vue';
import SubscriptionRuleForm from './SubscriptionRuleForm.vue';
import type { OptionItem } from '@/types/api';
import type { SubscriptionRule } from '@/types/workspace';
import {
  deleteSubscriptionRule,
  getSubscriptionRules,
  updateSubscriptionRule
} from '@/api/workspace';
import { composeSubscriptionPayload, parseRuleFilters, ruleFiltersToForm, splitKeywords } from '@/utils/subscriptionRule';
import { formatDateTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { resolveApiError } from '@/utils/apiError';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { confirm } = useConfirm();
const toast = useToast();

const statusFilterOptions: OptionItem[] = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];

const rows = ref<SubscriptionRule[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const status = ref('');
const loading = ref(false);
const error = ref('');
const togglingId = ref<number | null>(null);
const formOpen = ref(false);
const editing = ref<SubscriptionRule | null>(null);

function keywordsOf(rule: SubscriptionRule) {
  return splitKeywords(rule.keyword);
}

function conditionChips(rule: SubscriptionRule): string[] {
  const f = parseRuleFilters(rule.filtersJson);
  const chips: string[] = [];
  f.effectLevel?.forEach((v) => chips.push(`效力：${labelOf(v)}`));
  f.regionCode?.forEach((v) => chips.push(`地区：${v}`));
  f.authority?.forEach((v) => chips.push(`机关：${v}`));
  return chips;
}

function channelLabel(channel?: string) {
  return channel ? labelOf(channel === 'station' ? 'inbox' : channel) : '站内信';
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getSubscriptionRules({ pageNo: pageNo.value, pageSize, status: status.value || undefined });
    rows.value = res.list;
    total.value = res.total;
  } catch (err) {
    error.value = resolveApiError(err, '订阅规则读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editing.value = null;
  formOpen.value = true;
}

function openEdit(rule: SubscriptionRule) {
  editing.value = rule;
  formOpen.value = true;
}

function onSaved() {
  reload(editing.value ? pageNo.value : 1);
}

function goMatches(rule: SubscriptionRule) {
  router.push({ name: 'app.alerts', query: { ruleId: rule.id } });
}

async function toggle(rule: SubscriptionRule) {
  const nextStatus = rule.status === 'enabled' ? 'disabled' : 'enabled';
  const f = ruleFiltersToForm(rule.filtersJson);
  const payload = composeSubscriptionPayload(
    {
      ruleName: rule.ruleName || `规则 #${rule.id}`,
      ruleType: rule.ruleType,
      keyword: rule.keyword,
      deliveryChannel: rule.deliveryChannel,
      frequency: rule.frequency,
      status: nextStatus
    },
    f
  );
  togglingId.value = rule.id;
  try {
    await updateSubscriptionRule(rule.id, payload);
    rule.status = nextStatus;
    toast.success(nextStatus === 'enabled' ? '规则已启用。' : '规则已停用。');
  } catch (err) {
    toast.error(resolveApiError(err, '操作失败。'));
  } finally {
    togglingId.value = null;
  }
}

async function remove(rule: SubscriptionRule) {
  const ok = await confirm({
    title: '删除这条订阅规则？',
    message: '删除后将不再产生新的预警命中，已有命中记录保留。',
    confirmText: '确认删除',
    danger: true
  });
  if (!ok) return;
  try {
    await deleteSubscriptionRule(rule.id);
    toast.success('规则已删除。');
    await reload(pageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '删除失败。'));
  }
}

onMounted(() => reload(1));
</script>

<style scoped>
.rules {
  display: grid;
  gap: 20px;
}

.rules-head {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--ink);
}

.rules-head p {
  max-width: 72ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.65;
}

.rules-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rules-toolbar :deep(.x-select) {
  max-width: 200px;
}

.rules-count {
  margin-left: auto;
  color: var(--muted);
  font-size: var(--font-xs);
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.rule-card {
  display: grid;
  gap: 12px;
  padding: 18px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper-card);
}

.rule-card.off {
  background: var(--paper-2);
}

.rule-card__top {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.rule-card__name {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 20px;
  font-weight: 400;
  line-height: 1.2;
  color: var(--ink);
}

.rule-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  align-items: center;
}

.rule-card__tag {
  color: var(--muted);
  font-size: var(--font-xxs);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.rule-card__muted {
  color: var(--muted);
  font-size: 13px;
}

.rule-card__foot {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  color: var(--muted);
  font-size: var(--font-xs);
}

.rule-card__foot .dot {
  color: var(--muted-2);
}

.rule-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding-top: 4px;
  border-top: 1px solid var(--rule);
}

.rule-card__spacer {
  flex: 1;
}

@media (max-width: 860px) {
  .rule-grid {
    grid-template-columns: 1fr;
  }
}
</style>
