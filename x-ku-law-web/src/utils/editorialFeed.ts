import type { EditorialFeedItem, FeedChip } from '@/components/editorial/EditorialFeed.vue';
import type { NotificationInbox, SubscriptionMatch } from '@/types/workspace';
import { labelOf } from '@/utils/labels';

function formatTime(value?: string) {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return new Intl.DateTimeFormat('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(d);
}

export function matchToFeedItem(match: SubscriptionMatch): EditorialFeedItem {
  const unread = match.readStatus !== 'read' && match.readStatus !== 'READ';
  const chips: FeedChip[] = unread ? [{ label: '未读', tone: 'accent' }] : [{ label: '已读', tone: 'outline' }];
  return {
    id: match.id,
    title: match.documentTitle || `订阅命中 #${match.id}`,
    body: match.matchReason || '暂无命中说明。',
    meta: formatTime(match.createTime),
    chips,
    unread
  };
}

export function inboxToFeedItem(item: NotificationInbox): EditorialFeedItem {
  const unread = item.readStatus !== 'read' && item.readStatus !== 'READ';
  const chips: FeedChip[] = item.notificationType
    ? [{ label: labelOf(item.notificationType), tone: 'outline' }]
    : [];
  return {
    id: item.receiverId,
    title: item.title || `通知 #${item.receiverId}`,
    body: item.content || '暂无通知正文。',
    meta: formatTime(item.createTime),
    chips,
    unread
  };
}
