<template>
  <div class="tabs" role="tablist">
    <button
      v-for="(tab, idx) in tabs"
      :key="tabKey(tab, idx)"
      type="button"
      role="tab"
      class="tab"
      :class="{ active: idx === modelValue }"
      :aria-selected="idx === modelValue"
      @click="emit('update:modelValue', idx)"
    >
      <span class="mono tab-no">{{ tab.articleNo || `#${idx + 1}` }}</span>
      <span class="tab-title">{{ tab.articleTitle || tab.label || '条款' }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
export interface CompareTab {
  articleNo?: string;
  articleTitle?: string;
  label?: string;
}

defineProps<{
  tabs: CompareTab[];
  modelValue: number;
}>();

const emit = defineEmits<{
  'update:modelValue': [index: number];
}>();

function tabKey(tab: CompareTab, idx: number) {
  return `${tab.articleNo ?? ''}-${idx}`;
}
</script>

<style scoped>
.tabs {
  display: flex;
  flex-wrap: nowrap;
  gap: 0;
  overflow-x: auto;
  border-bottom: 1px solid var(--rule);
}

.tab {
  display: grid;
  gap: 4px;
  flex-shrink: 0;
  padding: 14px 20px 12px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}

.tab:hover {
  background: var(--paper-2);
}

.tab.active {
  border-bottom-color: var(--accent);
  color: var(--accent-deep);
}

.tab-no {
  font-size: var(--font-xxs);
  color: var(--muted);
}

.tab.active .tab-no {
  color: var(--accent);
}

.tab-title {
  font-family: var(--serif-body);
  font-size: 13px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
