<template>
  <div class="my-space">
    <nav class="my-space-tabs" aria-label="我的空间">
      <RouterLink
        v-for="tab in tabs"
        :key="tab.key"
        class="my-space-tab"
        :class="{ active: active === tab.key }"
        :to="{ name: 'app.mySpace', params: { resource: tab.key } }"
      >
        <component :is="tab.icon" :size="15" />
        <span>{{ tab.label }}</span>
      </RouterLink>
    </nav>

    <div class="my-space-body">
      <component :is="activeComponent" :key="active" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { Bookmark, FileSearch, MessageCircle } from '@lucide/vue';
import FavoritesView from '@/views/app/myspace/FavoritesView.vue';
import SavedSearchesView from '@/views/app/myspace/SavedSearchesView.vue';
import FeedbackView from '@/views/app/myspace/FeedbackView.vue';

/** tab.key 即子页路由段（/app/me/:resource），每个标签渲染各自的专属视图。 */
const tabs = [
  { key: 'favorites', label: '收藏夹', icon: Bookmark, component: FavoritesView },
  { key: 'savedSearches', label: '保存检索', icon: FileSearch, component: SavedSearchesView },
  { key: 'feedbacks', label: '我的反馈', icon: MessageCircle, component: FeedbackView }
];

const route = useRoute();
const active = computed(() => String(route.params.resource || 'favorites'));
const activeComponent = computed(() => tabs.find((t) => t.key === active.value)?.component || FavoritesView);
</script>

<style scoped>
.my-space-tabs {
  display: flex;
  gap: 6px;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 48px 0;
  border-bottom: 1px solid var(--rule);
}

.my-space-tab {
  display: inline-flex;
  gap: 7px;
  align-items: center;
  padding: 9px 14px;
  margin-bottom: -1px;
  border-bottom: 2px solid transparent;
  color: var(--ink-3);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.14s var(--ease), border-color 0.14s var(--ease);
}

.my-space-tab :deep(svg) {
  color: var(--muted);
  transition: color 0.14s var(--ease);
}

.my-space-tab:hover {
  color: var(--ink);
}

.my-space-tab.active {
  color: var(--accent-deep);
  border-bottom-color: var(--accent);
  font-weight: 600;
}

.my-space-tab.active :deep(svg) {
  color: var(--accent);
}

.my-space-body {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 28px 48px 48px;
  animation: page-in 0.45s var(--ease) both;
}

@media (max-width: 860px) {
  .my-space-tabs {
    padding: 16px 14px 0;
  }

  .my-space-body {
    padding: 20px 14px 40px;
  }
}
</style>
