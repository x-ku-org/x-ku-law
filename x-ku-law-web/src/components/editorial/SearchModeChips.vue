<template>
  <div class="mode-chips">
    <button
      v-for="mode in modes"
      :key="mode.key"
      type="button"
      class="mode-chip"
      @click="emit('select', mode.key)"
    >
      <strong>{{ mode.label }}</strong>
      <span>{{ mode.hint }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
export interface SearchMode {
  key: string;
  label: string;
  hint: string;
}

defineProps<{
  modes: SearchMode[];
}>();

const emit = defineEmits<{
  select: [key: string];
}>();
</script>

<style scoped>
.mode-chips {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mode-chip {
  display: grid;
  gap: 4px;
  padding: 14px 16px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.mode-chip:hover {
  border-color: var(--ink);
  background: var(--paper-2);
}

.mode-chip strong {
  font-family: var(--sans);
  font-size: 12px;
  font-weight: 600;
  color: var(--ink);
}

.mode-chip span {
  font-family: var(--serif-body);
  font-size: 13px;
  color: var(--ink-3);
  line-height: 1.4;
}

@media (max-width: 720px) {
  .mode-chips {
    grid-template-columns: 1fr;
  }
}
</style>
