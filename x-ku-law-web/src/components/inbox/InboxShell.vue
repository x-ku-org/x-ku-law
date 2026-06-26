<template>
  <section class="page inbox">
    <header class="inbox-head">
      <div>
        <div class="section-kicker">{{ kicker }}</div>
        <h1 class="h1">{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </div>
      <XButton
        variant="ghost"
        size="small"
        :loading="markAllLoading"
        :disabled="!unreadCount"
        @click="emit('mark-all')"
      >
        全部已读
      </XButton>
    </header>

    <nav class="inbox-tabs" aria-label="阅读状态筛选">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="inbox-tab"
        :class="{ active: activeTab === tab.key }"
        @click="emit('update:activeTab', tab.key)"
      >
        {{ tab.label }}
        <span v-if="tab.key === 'unread' && unreadCount" class="inbox-tab__count">{{ unreadCount }}</span>
      </button>
    </nav>

    <slot name="banner" />

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" :count="6" />
    <template v-else-if="total">
      <div class="inbox-list">
        <slot />
      </div>
      <XPagination
        class="inbox-pager"
        :total="total"
        :page-no="pageNo"
        :page-size="pageSize"
        @change="(p: number) => emit('change', p)"
      />
    </template>
    <EmptyState v-else :title="emptyTitle" :description="emptyDescription" />
  </section>
</template>

<script setup lang="ts">
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import XButton from '@/components/common/XButton.vue';
import XPagination from '@/components/common/XPagination.vue';

withDefaults(
  defineProps<{
    title: string;
    kicker?: string;
    description?: string;
    activeTab: string;
    unreadCount?: number;
    markAllLoading?: boolean;
    loading?: boolean;
    error?: string;
    total: number;
    pageNo: number;
    pageSize: number;
    emptyTitle?: string;
    emptyDescription?: string;
  }>(),
  {
    kicker: '',
    description: '',
    unreadCount: 0,
    markAllLoading: false,
    loading: false,
    error: '',
    emptyTitle: '暂无记录',
    emptyDescription: '当前筛选条件下没有内容。'
  }
);

const emit = defineEmits<{
  'update:activeTab': [key: string];
  'mark-all': [];
  change: [pageNo: number];
}>();

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'read', label: '已读' }
];
</script>

<style scoped>
.inbox {
  display: grid;
  gap: 18px;
}

.inbox-head {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--ink);
}

.inbox-head p {
  max-width: 72ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.65;
}

.inbox-tabs {
  display: flex;
  gap: 4px;
}

.inbox-tab {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: var(--control-h-sm);
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--ink-2);
  font-size: var(--font-xs);
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease), background 0.15s var(--ease);
}

.inbox-tab:hover {
  border-color: var(--ink);
  color: var(--ink);
}

.inbox-tab.active {
  border-color: var(--accent);
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.inbox-tab__count {
  display: inline-grid;
  place-items: center;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: 9px;
  background: var(--accent);
  color: var(--paper);
  font-size: 10px;
  line-height: 1;
}

.inbox-list {
  border-top: 1px solid var(--ink);
}

.inbox-pager {
  margin-top: 14px;
}
</style>
