<template>
  <Teleport to="body">
    <div class="toast-host" aria-live="polite" aria-atomic="true">
      <TransitionGroup name="toast">
        <article v-for="toast in toasts" :key="toast.id" class="toast" :class="`toast--${toast.tone}`">
          <div class="toast-copy">
            <span class="toast-label">{{ labels[toast.tone] }}</span>
            <p>{{ toast.message }}</p>
          </div>
          <button type="button" class="toast-close" aria-label="关闭提示" @click="dismiss(toast.id)">×</button>
        </article>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast';

const { toasts, dismiss } = useToast();
const labels = {
  info: '提示',
  success: '成功',
  error: '错误'
} as const;
</script>

<style scoped>
.toast-host {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  display: grid;
  gap: 10px;
  width: min(360px, calc(100vw - 24px));
}

.toast {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: color-mix(in oklch, var(--paper-card), white 30%);
  box-shadow: none;
  backdrop-filter: none;
}

.toast--success {
  border-color: color-mix(in oklch, var(--moss), transparent 68%);
}

.toast--error {
  border-color: color-mix(in oklch, var(--rose), transparent 58%);
}

.toast--info {
  border-color: color-mix(in oklch, var(--accent), transparent 70%);
}

.toast-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.toast-label {
  color: var(--ink-3);
  font-family: var(--sans);
  font-size: var(--font-xs);
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1.4;
}

.toast--success .toast-label {
  color: var(--moss);
}

.toast--error .toast-label {
  color: var(--rose);
}

.toast--info .toast-label {
  color: var(--accent-deep);
}

.toast p {
  margin: 0;
  color: var(--ink);
  font-family: var(--sans);
  font-size: 14px;
  font-weight: 400;
  line-height: 1.55;
}

.toast-close {
  flex-shrink: 0;
  border: 0;
  background: transparent;
  color: var(--muted-2);
  cursor: pointer;
  font-family: var(--sans);
  font-size: 16px;
  line-height: 1;
  padding: 2px 0 0;
  transition: color 0.15s var(--ease);
}

.toast-close:hover {
  color: var(--ink-2);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s var(--ease), transform 0.2s var(--ease);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@media (max-width: 860px) {
  .toast-host {
    right: 12px;
    bottom: 12px;
    width: calc(100vw - 24px);
  }
}
</style>
