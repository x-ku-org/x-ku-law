<template>
  <label class="field" :class="{ 'field--error': error }">
    <span class="label">
      {{ label }}
      <b v-if="required">*</b>
    </span>
    <slot />
    <small v-if="hint">{{ hint }}</small>
    <small v-if="error" class="error">{{ error }}</small>
  </label>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  required?: boolean;
  hint?: string;
  error?: string;
}>();
</script>

<style scoped>
.field {
  display: grid;
  gap: 7px;
  min-width: 0;
}

.field--error :deep(input),
.field--error :deep(select),
.field--error :deep(.x-select__trigger),
.field--error :deep(textarea) {
  border-color: var(--rose);
}

.label {
  color: var(--ink-2);
  font-size: 12px;
  font-weight: 600;
}

b {
  color: var(--rose);
  font-weight: 600;
}

small {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.4;
}

.error {
  color: var(--rose);
}
</style>
