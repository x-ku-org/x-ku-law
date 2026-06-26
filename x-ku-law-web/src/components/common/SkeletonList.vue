<template>
  <div class="sk-list" :class="{ 'sk-list--search': variant === 'search' }">
    <div v-for="n in count" :key="n" class="sk-list-item">
      <span class="sk-list-ordinal sk-shimmer" />
      <div class="sk-list-body">
        <template v-if="variant === 'search'">
          <div class="sk-title-row">
            <Skeleton width="62%" />
            <span class="sk-badge sk-shimmer" />
          </div>
          <div class="sk-meta-row">
            <span v-for="m in 4" :key="m" class="sk-meta-chip sk-shimmer" :style="{ width: metaWidth(m) }" />
          </div>
          <Skeleton width="78%" />
        </template>
        <template v-else>
          <Skeleton width="68%" />
          <Skeleton width="92%" />
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';

withDefaults(
  defineProps<{
    count?: number;
    variant?: 'line' | 'search';
  }>(),
  {
    count: 3,
    variant: 'line'
  }
);

function metaWidth(index: number) {
  const widths = ['88px', '64px', '112px', '96px'];
  return widths[(index - 1) % widths.length];
}
</script>

<style scoped>
.sk-list {
  display: grid;
  gap: 0;
  border-top: 1px solid var(--rule);
}

.sk-list--search {
  border-top: 1px solid var(--ink);
}

.sk-list-item {
  display: flex;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--rule);
}

.sk-list--search .sk-list-item {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 20px;
  padding: 24px 0;
}

.sk-list-ordinal {
  flex-shrink: 0;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 1px;
}

.sk-list--search .sk-list-ordinal {
  width: 28px;
  height: 14px;
  margin-top: 4px;
  border-radius: 2px;
}

.sk-list-body {
  display: grid;
  flex: 1;
  gap: 8px;
  min-width: 0;
}

.sk-list--search .sk-list-body {
  gap: 10px;
}

.sk-title-row {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.sk-title-row :deep(.sk) {
  flex: 1;
  min-width: 0;
}

.sk-badge {
  flex-shrink: 0;
  width: 56px;
  height: 22px;
  border-radius: 4px;
}

.sk-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
}

.sk-meta-chip {
  height: 10px;
  border-radius: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .sk-list-ordinal,
  .sk-badge,
  .sk-meta-chip {
    background: var(--paper-sunk);
  }
}

@media (max-width: 720px) {
  .sk-list--search .sk-list-item {
    grid-template-columns: 1fr;
  }
}
</style>
