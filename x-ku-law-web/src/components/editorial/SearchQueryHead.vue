<template>
  <header class="search-query-head">
    <div class="section-kicker">§ 检索 · 全库索引</div>
    <form class="query-row" @submit.prevent="emit('submit')">
      <input
        :value="modelValue"
        class="query-input"
        :placeholder="placeholder"
        autofocus
        @input="onInput"
      />
      <span class="query-hint mono">↵ ENTER</span>
    </form>
    <p v-if="showStats && !statsLoading" class="query-stats mono">
      <span class="num">{{ hitCount }}</span> 条命中
      <template v-if="docCount != null"> · 跨 <span class="num">{{ docCount }}</span> 件文件</template>
      <template v-if="elapsedMs != null"> · 用时 {{ (elapsedMs / 1000).toFixed(2) }}s</template>
    </p>
    <span v-else-if="showStats && statsLoading" class="query-stats-sk sk-shimmer" aria-hidden="true" />
  </header>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: string;
    placeholder?: string;
    hitCount?: number;
    docCount?: number;
    elapsedMs?: number;
    showStats?: boolean;
    statsLoading?: boolean;
  }>(),
  {
    placeholder: '输入关键词、文号、发布机关或条款内容',
    hitCount: 0,
    docCount: undefined,
    elapsedMs: undefined,
    showStats: true,
    statsLoading: false
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
  submit: [];
}>();

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value);
}
</script>

<style scoped>
.search-query-head {
  display: grid;
  gap: 14px;
  margin-bottom: 26px;
}

.query-row {
  position: relative;
}

.query-input {
  width: 100%;
  padding: 0 72px 12px 0;
  border: 0;
  border-bottom: 2px solid var(--ink);
  background: transparent;
  color: var(--ink);
  font-family: var(--serif-display);
  font-size: clamp(36px, 5vw, 56px);
  font-style: italic;
  letter-spacing: -0.015em;
  line-height: 1;
  outline: none;
}

.query-input:focus {
  border-bottom-color: var(--accent);
}

.query-hint {
  position: absolute;
  top: 8px;
  right: 0;
  color: var(--muted);
  font-size: var(--font-xs);
}

.query-stats {
  margin: 0;
  color: var(--ink-2);
  font-size: 12px;
}

.query-stats-sk {
  display: block;
  width: 220px;
  height: 12px;
  border-radius: 2px;
}
</style>
