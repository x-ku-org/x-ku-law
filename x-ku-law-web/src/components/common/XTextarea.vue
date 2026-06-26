<template>
  <textarea
    v-bind="$attrs"
    class="x-textarea"
    :class="{ 'x-textarea--invalid': invalid }"
    :value="modelValue"
    :aria-invalid="invalid ? 'true' : undefined"
    @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
  />
</template>

<script setup lang="ts">
defineOptions({ inheritAttrs: false });

withDefaults(
  defineProps<{
    modelValue?: string;
    invalid?: boolean;
  }>(),
  {
    modelValue: '',
    invalid: false
  }
);
defineEmits<{ 'update:modelValue': [value: string] }>();
</script>

<style scoped>
.x-textarea {
  width: 100%;
  min-height: 96px;
  padding: 10px 12px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink);
  line-height: 1.6;
  resize: vertical;
  outline: 0;
}

.x-textarea:focus {
  border-color: var(--ink);
}

.x-textarea--invalid,
.x-textarea--invalid:focus {
  border-color: var(--rose);
}

.x-textarea:disabled {
  background: var(--paper-sunk);
  color: var(--muted);
  cursor: not-allowed;
}
</style>
