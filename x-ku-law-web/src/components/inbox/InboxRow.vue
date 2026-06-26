<template>
  <article class="inbox-row" :class="{ unread, expanded }">
    <button type="button" class="inbox-row__main" :aria-expanded="expandable ? expanded : undefined" @click="emit('select')">
      <span class="inbox-row__body">
        <div v-if="chips.length || meta" class="inbox-row__meta">
          <span v-if="chips.length" class="inbox-row__chips">
            <XChip v-for="chip in chips" :key="chip.label" :tone="chip.tone">{{ chip.label }}</XChip>
          </span>
          <span v-if="meta" class="mono inbox-row__time">{{ meta }}</span>
        </div>
        <span class="inbox-row__title">{{ title }}</span>
        <span v-if="body" class="inbox-row__desc" :class="{ clamp: !expanded }">{{ body }}</span>
      </span>
      <span class="inbox-row__arrow" aria-hidden="true">{{ expandable ? (expanded ? '−' : '+') : '→' }}</span>
    </button>

    <div v-if="expanded && $slots.expanded" class="inbox-row__expanded">
      <slot name="expanded" />
    </div>

    <div v-if="$slots.actions" class="inbox-row__actions">
      <slot name="actions" />
    </div>
  </article>
</template>

<script setup lang="ts">
import XChip from '@/components/common/XChip.vue';
import type { FeedChip } from '@/components/editorial/EditorialFeed.vue';

withDefaults(
  defineProps<{
    title: string;
    body?: string;
    meta?: string;
    chips?: FeedChip[];
    unread?: boolean;
    /** 可展开（消息中心点击就地展开正文）；否则箭头表示跳转 */
    expandable?: boolean;
    expanded?: boolean;
  }>(),
  { body: '', meta: '', chips: () => [], unread: false, expandable: false, expanded: false }
);

const emit = defineEmits<{ select: [] }>();
</script>

<style scoped>
.inbox-row {
  border-bottom: 1px solid var(--rule);
}

.inbox-row.unread {
  border-left: 3px solid var(--accent);
  margin-left: -14px;
  padding-left: 14px;
  background: linear-gradient(90deg, var(--accent-soft) 0%, transparent 48%);
}

.inbox-row__main {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 14px;
  width: 100%;
  padding: 18px 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.inbox-row__main:hover {
  background: var(--paper-2);
}

.inbox-row__body {
  min-width: 0;
}

.inbox-row__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.inbox-row__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.inbox-row__time {
  flex-shrink: 0;
  margin-left: auto;
  color: var(--muted);
  font-size: var(--font-xs);
}

.inbox-row__title {
  display: block;
  font-family: var(--serif-display);
  font-size: 18px;
  font-weight: 400;
  line-height: 1.25;
  color: var(--ink);
}

.inbox-row__desc {
  display: block;
  margin-top: 5px;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-3);
  white-space: pre-wrap;
}

.inbox-row__desc.clamp {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  white-space: normal;
}

.inbox-row__arrow {
  align-self: center;
  color: var(--muted-2);
  font-family: var(--mono);
  font-size: 15px;
}

.inbox-row__expanded {
  padding: 0 0 16px;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.inbox-row__actions {
  display: flex;
  gap: 8px;
  padding: 0 0 16px;
}
</style>
