<template>
  <span
    class="loading-ring"
    :class="[`loading-ring--${size}`, { 'loading-ring--static': !animate }]"
    :style="{ '--ring-size': ringSize }"
    role="status"
    :aria-label="label"
  >
    <svg class="loading-ring-svg" viewBox="0 0 20 20" aria-hidden="true">
      <circle class="loading-ring-track" cx="10" cy="10" r="8" />
      <circle class="loading-ring-arc" cx="10" cy="10" r="8" />
    </svg>
    <span class="loading-ring-core" aria-hidden="true" />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    size?: 'sm' | 'md' | 'lg';
    label?: string;
    animate?: boolean;
  }>(),
  {
    size: 'md',
    label: '正在载入',
    animate: true
  }
);

const ringSize = computed(() => {
  if (props.size === 'sm') return '14px';
  if (props.size === 'lg') return '28px';
  return '18px';
});
</script>

<style scoped>
.loading-ring {
  --ring-size: 18px;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: var(--ring-size);
  height: var(--ring-size);
  flex: 0 0 auto;
}

.loading-ring-svg {
  width: 100%;
  height: 100%;
  animation: loading-ring-spin 0.85s linear infinite;
}

.loading-ring-track {
  fill: none;
  stroke: var(--rule-strong);
  stroke-width: 1.5;
}

.loading-ring-arc {
  fill: none;
  stroke: var(--accent);
  stroke-width: 1.5;
  stroke-linecap: round;
  stroke-dasharray: 34 16;
  stroke-dashoffset: 0;
}

.loading-ring-core {
  position: absolute;
  width: calc(var(--ring-size) * 0.22);
  height: calc(var(--ring-size) * 0.22);
  background: var(--accent);
  transform: rotate(45deg);
  opacity: 0.92;
}

.loading-ring--sm {
  --ring-size: 14px;
}

.loading-ring--lg {
  --ring-size: 28px;
}

.loading-ring--static .loading-ring-svg {
  animation: none;
}

.loading-ring--static .loading-ring-arc {
  stroke-dasharray: 20 30;
  opacity: 0.75;
}

@keyframes loading-ring-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .loading-ring-svg {
    animation: none;
  }

  .loading-ring-arc {
    stroke-dasharray: 20 30;
    opacity: 0.8;
  }
}
</style>
