<template>
  <div class="feed" :class="{ 'feed--bordered': bordered }">
    <button
      v-for="(item, index) in items"
      :key="itemKey(item, index)"
      type="button"
      class="feed-item"
      :class="{ unread: item.unread }"
      @click="emit('select', item, index)"
    >
      <div class="feed-ordinal num">{{ String(index + 1).padStart(2, '0') }}</div>
      <div class="feed-body">
        <div v-if="item.chips?.length || item.meta" class="feed-meta">
          <XChip v-for="chip in item.chips" :key="chip.label" :tone="chip.tone">{{ chip.label }}</XChip>
          <span v-if="item.meta" class="mono feed-time">{{ item.meta }}</span>
        </div>
        <h3 class="feed-title">{{ item.title }}</h3>
        <p v-if="item.body" class="feed-desc">{{ item.body }}</p>
      </div>
      <span class="feed-arrow" aria-hidden="true">→</span>
    </button>
    <EmptyState v-if="!items.length && !loading" :title="emptyTitle" :description="emptyDescription" />
  </div>
</template>

<script setup lang="ts">
import EmptyState from '@/components/common/EmptyState.vue';
import XChip from '@/components/common/XChip.vue';

export interface FeedChip {
  label: string;
  tone?: 'accent' | 'gold' | 'rose' | 'moss' | 'outline';
}

export interface EditorialFeedItem {
  id?: string | number;
  title: string;
  body?: string;
  meta?: string;
  chips?: FeedChip[];
  unread?: boolean;
}

withDefaults(
  defineProps<{
    items: EditorialFeedItem[];
    loading?: boolean;
    bordered?: boolean;
    emptyTitle?: string;
    emptyDescription?: string;
  }>(),
  {
    loading: false,
    bordered: true,
    emptyTitle: '暂无记录',
    emptyDescription: '当前没有可展示的内容。'
  }
);

const emit = defineEmits<{
  select: [item: EditorialFeedItem, index: number];
}>();

function itemKey(item: EditorialFeedItem, index: number) {
  return item.id ?? index;
}
</script>

<style scoped>
.feed--bordered {
  border-top: 1px solid var(--ink);
}

.feed-item {
  display: grid;
  grid-template-columns: 46px 1fr auto;
  gap: 16px;
  width: 100%;
  padding: 20px 0 22px;
  border: 0;
  border-bottom: 1px solid var(--rule);
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.feed-item:hover {
  background: var(--paper-2);
}

.feed-item.unread {
  border-left: 2px solid var(--accent);
  padding-left: 12px;
  margin-left: -12px;
}

.feed-ordinal {
  text-align: right;
  font-size: 28px;
  line-height: 1;
  color: var(--muted-2);
}

.feed-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.feed-time {
  color: var(--muted);
}

.feed-title {
  margin: 0 0 6px;
  font-family: var(--serif-display);
  font-size: 19px;
  font-weight: 400;
  letter-spacing: -0.005em;
  line-height: 1.2;
  color: var(--ink);
}

.feed-desc {
  margin: 0;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.55;
  color: var(--ink-3);
}

.feed-arrow {
  align-self: center;
  color: var(--muted-2);
  font-family: var(--mono);
  font-size: 14px;
}
</style>
