<template>
  <svg class="cube" :width="size" :height="size" viewBox="-50 -50 100 100" aria-hidden="true">
    <g :class="{ spin }">
      <path d="M0-40 34.6-20v40L0 40-34.6 20v-40Z" fill="none" stroke="var(--paper-2)" stroke-width="0.5" opacity="0.45" />
      <path d="M-22-12 0-24l22 12L0 0Z" fill="var(--paper-2)" opacity="0.85" />
      <path d="M-22-12v24L0 24V0Z" fill="var(--paper-2)" opacity="0.55" />
      <path d="M22-12v24L0 24V0Z" fill="var(--paper-2)" opacity="0.35" />
      <path d="m0 4 9 5-9 5-9-5Z" fill="var(--accent)" opacity="0.95" />
    </g>
  </svg>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ size?: number; spin?: boolean }>(), {
  size: 220,
  spin: true
});
</script>

<style scoped>
.cube {
  display: block;
  overflow: visible;
}

.spin {
  transform-origin: center;
  animation: spin 60s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
