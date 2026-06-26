<template>
  <img
    :src="logoSrc"
    :width="size"
    :height="size"
    class="xku-logo"
    :class="{ 'xku-logo--animated': animated }"
    alt=""
    aria-hidden="true"
  />
</template>

<script setup lang="ts">
import logoSrc from '../../../xku.png';

withDefaults(defineProps<{ size?: number; animated?: boolean }>(), {
  size: 28,
  animated: false
});
</script>

<style scoped>
.xku-logo {
  display: block;
  object-fit: contain;
}

.xku-logo--animated {
  animation: xku-logo-pulse 3s ease-in-out infinite;
}

@keyframes xku-logo-pulse {
  0%,
  100% {
    opacity: 0.82;
  }
  50% {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .xku-logo--animated {
    animation: none;
  }
}
</style>
