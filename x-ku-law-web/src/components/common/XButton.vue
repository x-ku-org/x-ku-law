<template>
  <button
    class="x-button"
    :class="[`x-button--${variant}`, { 'x-button--small': size === 'small', 'x-button--loading': loading }]"
    :type="type"
    :disabled="disabled || loading"
    :aria-busy="loading ? 'true' : undefined"
  >
    <span v-if="loading" class="x-button-spinner" aria-hidden="true" />
    <span class="x-button-content">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'default' | 'primary' | 'accent' | 'ghost' | 'danger';
    size?: 'default' | 'small';
    type?: 'button' | 'submit' | 'reset';
    loading?: boolean;
    disabled?: boolean;
  }>(),
  {
    variant: 'default',
    size: 'default',
    type: 'button',
    loading: false,
    disabled: false
  }
);
</script>

<style scoped>
.x-button {
  box-sizing: border-box;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  height: var(--control-h);
  white-space: nowrap;
  padding: 0 14px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: var(--paper-card);
  color: var(--ink);
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 1;
  transition:
    border-color 0.15s var(--ease),
    background 0.15s var(--ease),
    color 0.15s var(--ease),
    transform 0.15s var(--ease);
}

.x-button:hover {
  border-color: var(--ink);
}

.x-button:active:not(:disabled) {
  transform: translateY(1px);
}

.x-button-content {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
}

.x-button-spinner {
  width: 10px;
  height: 10px;
  border: 1px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: x-button-spin 0.8s linear infinite;
}

.x-button--primary {
  border-color: var(--ink);
  background: var(--ink);
  color: var(--paper);
}

.x-button--accent {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

/* 危险/删除：rose（见 DESIGN.md「rose = 警示、删除」） */
.x-button--danger {
  border-color: var(--rose);
  background: var(--rose);
  color: var(--paper);
}

.x-button--ghost {
  border-color: transparent;
  background: transparent;
  color: var(--ink-2);
}

.x-button--ghost:hover {
  background: var(--accent-soft);
  color: var(--ink);
}

.x-button--small {
  height: var(--control-h-sm);
  padding: 0 10px;
  font-size: var(--font-xs);
}

.x-button:disabled {
  border-color: var(--rule);
  background: var(--paper-sunk);
  color: var(--muted);
  cursor: not-allowed;
}

.x-button--primary:disabled,
.x-button--accent:disabled,
.x-button--danger:disabled {
  opacity: 0.74;
}

@keyframes x-button-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .x-button,
  .x-button-spinner {
    transition: none;
    animation: none;
  }
}
</style>
