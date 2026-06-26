<template>
  <label class="x-checkbox">
    <input class="x-checkbox__input" type="checkbox" :checked="Boolean(modelValue)" @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)" />
    <span class="x-checkbox__mark" aria-hidden="true" />
    <span class="x-checkbox__label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue?: boolean;
    label?: string;
  }>(),
  {
    modelValue: false,
    label: ''
  }
);

defineEmits<{ 'update:modelValue': [value: boolean] }>();
</script>

<style scoped>
.x-checkbox {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  min-height: var(--control-h);
  color: var(--ink);
  cursor: pointer;
}

.x-checkbox__input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.x-checkbox__mark {
  position: relative;
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.x-checkbox__mark::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 5px;
  width: 5px;
  height: 9px;
  border-right: 2px solid var(--paper);
  border-bottom: 2px solid var(--paper);
  opacity: 0;
  transform: rotate(45deg);
  transition: opacity 0.15s var(--ease);
}

.x-checkbox__input:checked + .x-checkbox__mark {
  border-color: var(--ink);
  background: var(--ink);
}

.x-checkbox__input:checked + .x-checkbox__mark::after {
  opacity: 1;
}

.x-checkbox__input:focus-visible + .x-checkbox__mark {
  outline: 2px solid color-mix(in oklch, var(--accent), transparent 55%);
  outline-offset: 2px;
}

.x-checkbox__label {
  font-size: 14px;
  line-height: 1.5;
}
</style>
