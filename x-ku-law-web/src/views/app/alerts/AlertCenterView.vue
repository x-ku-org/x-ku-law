<template>
  <InboxShell
    title="预警中心"
    kicker="§ Alerts"
    description="订阅规则命中的法规变化都会汇总到这里，可逐条标记已读或一键清空。"
    :active-tab="activeTab"
    :unread-count="unreadCount"
    :mark-all-loading="markAllLoading"
    :loading="loading"
    :error="error"
    :total="total"
    :page-no="pageNo"
    :page-size="pageSize"
    :empty-title="emptyTitle"
    :empty-description="emptyDescription"
    @update:active-tab="onTab"
    @mark-all="markAll"
    @change="reload"
  >
    <template #banner>
      <div v-if="ruleId" class="alert-banner">
        <span class="mono">来自规则 #{{ ruleId }}</span>
        <button type="button" @click="clearRule">清除筛选</button>
      </div>
    </template>

    <template v-for="group in groups" :key="group.key">
      <div class="inbox-group">{{ group.label }}</div>
      <InboxRow
        v-for="match in group.items"
        :key="match.id"
        :title="match.documentTitle || `订阅命中 #${match.id}`"
        :body="match.matchReason || '暂无命中说明。'"
        :meta="formatInboxTime(match.matchedTime || match.createTime)"
        :chips="chipsFor(match)"
        :unread="isUnread(match)"
        @select="openMatch(match)"
      >
        <template v-if="isUnread(match)" #actions>
          <XButton size="small" variant="ghost" @click="markOne(match)">标记已读</XButton>
        </template>
      </InboxRow>
    </template>
  </InboxShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import InboxShell from '@/components/inbox/InboxShell.vue';
import InboxRow from '@/components/inbox/InboxRow.vue';
import XButton from '@/components/common/XButton.vue';
import type { FeedChip } from '@/components/editorial/EditorialFeed.vue';
import {
  getSubscriptionMatches,
  getSubscriptionMatchUnreadCount,
  markAllSubscriptionMatchesRead,
  markSubscriptionMatchRead
} from '@/api/workspace';
import type { SubscriptionMatch } from '@/types/workspace';
import { groupByDate } from '@/utils/dateGroups';
import { formatInboxTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { lawDetailTo } from '@/router/navigation';
import { resolveApiError } from '@/utils/apiError';
import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const toast = useToast();

const activeTab = ref('all');
const rows = ref<SubscriptionMatch[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const loading = ref(false);
const error = ref('');
const unreadCount = ref(0);
const markAllLoading = ref(false);

const ruleId = computed(() => {
  const v = Number(route.query.ruleId);
  return Number.isFinite(v) && v > 0 ? v : undefined;
});

const groups = computed(() => groupByDate(rows.value, (m) => m.matchedTime || m.createTime));
const emptyTitle = computed(() =>
  activeTab.value === 'unread' ? '暂无未读预警' : '暂无预警记录'
);
const emptyDescription = computed(() =>
  activeTab.value === 'unread'
    ? '订阅命中已全部处理。'
    : '配置订阅规则后，命中的法规变化会出现在这里。'
);

function isUnread(m: SubscriptionMatch) {
  return m.readStatus !== 'read' && m.readStatus !== 'READ';
}

function chipsFor(m: SubscriptionMatch): FeedChip[] {
  const chips: FeedChip[] = [];
  if (m.matchType) chips.push({ label: labelOf(m.matchType), tone: 'outline' });
  chips.push(isUnread(m) ? { label: '未读', tone: 'accent' } : { label: '已读', tone: 'outline' });
  return chips;
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getSubscriptionMatches({
      pageNo: pageNo.value,
      pageSize,
      readStatus: activeTab.value === 'all' ? undefined : activeTab.value,
      ruleId: ruleId.value
    });
    rows.value = res.list;
    total.value = res.total;
  } catch (err) {
    error.value = resolveApiError(err, '预警读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

async function refreshUnread() {
  try {
    unreadCount.value = await getSubscriptionMatchUnreadCount();
  } catch {
    /* 角标失败不阻断主流程 */
  }
}

function onTab(key: string) {
  if (key === activeTab.value) return;
  activeTab.value = key;
  reload(1);
}

function clearRule() {
  router.replace({ name: 'app.alerts' });
}

async function markOne(m: SubscriptionMatch) {
  try {
    await markSubscriptionMatchRead(m.id);
    await Promise.all([reload(pageNo.value), refreshUnread()]);
  } catch (err) {
    toast.error(resolveApiError(err, '标记已读失败。'));
  }
}

async function openMatch(m: SubscriptionMatch) {
  if (isUnread(m)) {
    try {
      await markSubscriptionMatchRead(m.id);
      void refreshUnread();
    } catch {
      /* 标记失败不阻断跳转 */
    }
  }
  if (m.documentId) {
    router.push(lawDetailTo(m.documentId));
  } else {
    await reload(pageNo.value);
  }
}

async function markAll() {
  markAllLoading.value = true;
  try {
    const n = await markAllSubscriptionMatchesRead();
    toast.success(n > 0 ? `已标记 ${n} 条为已读。` : '没有未读预警。');
    await Promise.all([reload(1), refreshUnread()]);
  } catch (err) {
    toast.error(resolveApiError(err, '操作失败。'));
  } finally {
    markAllLoading.value = false;
  }
}

watch(ruleId, () => reload(1));

onMounted(() => {
  reload(1);
  refreshUnread();
});
</script>

<style scoped>
.inbox-group {
  padding: 16px 0 6px;
  color: var(--muted);
  font-family: var(--sans);
  font-size: var(--font-xxs);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.alert-banner {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid var(--rule);
  border-radius: var(--radius-control);
  background: var(--paper-2);
}

.alert-banner .mono {
  color: var(--ink-2);
  font-size: var(--font-xs);
}

.alert-banner button {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: var(--accent-deep);
  font-size: 12px;
  cursor: pointer;
}

.alert-banner button:hover {
  color: var(--ink);
}
</style>
