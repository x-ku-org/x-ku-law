<template>
  <InboxShell
    title="消息中心"
    kicker="§ Messages"
    description="站内通知、任务提醒和系统公告都在这里，点击展开正文或前往关联法规。"
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
    <template v-for="group in groups" :key="group.key">
      <div class="inbox-group">{{ group.label }}</div>
      <InboxRow
        v-for="item in group.items"
        :key="item.receiverId"
        :title="item.title || `通知 #${item.receiverId}`"
        :body="item.content || '暂无通知正文。'"
        :meta="formatInboxTime(item.createTime)"
        :chips="chipsFor(item)"
        :unread="isUnread(item)"
        :expandable="!isLawLink(item)"
        :expanded="expanded.has(item.receiverId)"
        @select="onSelect(item)"
      >
        <template v-if="isLawLink(item)" #actions>
          <XButton size="small" variant="ghost" @click="goLaw(item)">前往法规</XButton>
          <XButton v-if="isUnread(item)" size="small" variant="ghost" @click="markOne(item)">标记已读</XButton>
        </template>
      </InboxRow>
    </template>
  </InboxShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import InboxShell from '@/components/inbox/InboxShell.vue';
import InboxRow from '@/components/inbox/InboxRow.vue';
import XButton from '@/components/common/XButton.vue';
import type { FeedChip } from '@/components/editorial/EditorialFeed.vue';
import {
  getInbox,
  getNotificationUnreadCount,
  markAllNotificationsRead,
  markNotificationRead
} from '@/api/workspace';
import type { NotificationInbox } from '@/types/workspace';
import { groupByDate } from '@/utils/dateGroups';
import { formatInboxTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { lawDetailTo } from '@/router/navigation';
import { resolveApiError } from '@/utils/apiError';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const toast = useToast();

const activeTab = ref('all');
const rows = ref<NotificationInbox[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const loading = ref(false);
const error = ref('');
const unreadCount = ref(0);
const markAllLoading = ref(false);
const expanded = ref<Set<number>>(new Set());

const groups = computed(() => groupByDate(rows.value, (n) => n.createTime));
const emptyTitle = computed(() => (activeTab.value === 'unread' ? '暂无未读消息' : '暂无消息'));
const emptyDescription = computed(() =>
  activeTab.value === 'unread' ? '通知都已读完。' : '系统通知与任务提醒会出现在这里。'
);

function isUnread(n: NotificationInbox) {
  return n.readStatus !== 'read' && n.readStatus !== 'READ';
}

function isLawLink(n: NotificationInbox) {
  return n.refType === 'law_document' && Boolean(n.refId);
}

function chipsFor(n: NotificationInbox): FeedChip[] {
  if (!n.notificationType) return [];
  return [{ label: labelOf(n.notificationType), tone: 'outline' }];
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getInbox({
      pageNo: pageNo.value,
      pageSize,
      readStatus: activeTab.value === 'all' ? undefined : activeTab.value
    });
    rows.value = res.list;
    total.value = res.total;
  } catch (err) {
    error.value = resolveApiError(err, '消息读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

async function refreshUnread() {
  try {
    unreadCount.value = await getNotificationUnreadCount();
  } catch {
    /* 角标失败不阻断主流程 */
  }
}

function onTab(key: string) {
  if (key === activeTab.value) return;
  activeTab.value = key;
  expanded.value = new Set();
  reload(1);
}

async function markReadSilently(n: NotificationInbox) {
  if (!isUnread(n) || !n.notificationId) return;
  try {
    await markNotificationRead(n.notificationId);
    n.readStatus = 'read';
    void refreshUnread();
  } catch {
    /* 静默失败 */
  }
}

async function onSelect(n: NotificationInbox) {
  if (isLawLink(n)) {
    await goLaw(n);
    return;
  }
  const next = new Set(expanded.value);
  if (next.has(n.receiverId)) {
    next.delete(n.receiverId);
  } else {
    next.add(n.receiverId);
    await markReadSilently(n);
  }
  expanded.value = next;
}

async function goLaw(n: NotificationInbox) {
  await markReadSilently(n);
  if (n.refId) router.push(lawDetailTo(n.refId));
}

async function markOne(n: NotificationInbox) {
  await markReadSilently(n);
  if (activeTab.value === 'unread') await reload(pageNo.value);
}

async function markAll() {
  markAllLoading.value = true;
  try {
    const count = await markAllNotificationsRead();
    toast.success(count > 0 ? `已标记 ${count} 条为已读。` : '没有未读消息。');
    await Promise.all([reload(1), refreshUnread()]);
  } catch (err) {
    toast.error(resolveApiError(err, '操作失败。'));
  } finally {
    markAllLoading.value = false;
  }
}

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
</style>
