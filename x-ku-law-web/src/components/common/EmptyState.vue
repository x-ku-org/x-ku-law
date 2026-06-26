<template>
  <div class="empty">
    <Diamond :size="9" />
    <div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <div v-if="$slots.default" class="empty-actions">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Diamond from '@/components/brand/Diamond.vue';

withDefaults(
  defineProps<{
    title?: string;
    description?: string;
  }>(),
  {
    title: '暂无数据',
    description: '当前没有可展示内容。'
  }
);
</script>

<style scoped>
.empty {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 28px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

h3 {
  margin: 0;
  color: var(--ink);
  font-family: var(--serif-body);
  font-size: 18px;
  font-weight: 500;
}

p {
  max-width: 56ch;
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.empty-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}
</style>
