<template>
  <input
    v-bind="$attrs"
    class="x-input"
    :class="{ 'x-input--invalid': invalid }"
    :value="modelValue"
    :aria-invalid="invalid ? 'true' : undefined"
    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>

<script setup lang="ts">
defineOptions({ inheritAttrs: false });

withDefaults(
  defineProps<{
    modelValue?: string | number;
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
.x-input {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  height: var(--control-h);
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: var(--paper);
  color: var(--ink);
  line-height: 1;
  outline: 0;
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.x-input:hover {
  border-color: var(--muted-2);
}

.x-input:focus {
  border-color: var(--ink);
}

.x-input--invalid,
.x-input--invalid:focus {
  border-color: var(--rose);
}

.x-input:disabled {
  background: var(--paper-sunk);
  color: var(--muted);
  cursor: not-allowed;
}
</style>
