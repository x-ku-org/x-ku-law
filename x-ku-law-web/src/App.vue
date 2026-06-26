<template>
  <RouterView v-slot="{ Component }">
    <Transition name="route-fade" mode="out-in">
      <component :is="Component" />
    </Transition>
  </RouterView>
  <XToastHost />
  <XConfirmDialog />
</template>

<script setup lang="ts">
import XConfirmDialog from '@/components/common/XConfirmDialog.vue';
import XToastHost from '@/components/common/XToastHost.vue';
</script>

<style>
.route-fade-enter-active,
.route-fade-leave-active {
  transition: opacity 0.18s var(--ease), transform 0.18s var(--ease);
}

.route-fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}

.route-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (prefers-reduced-motion: reduce) {
  .route-fade-enter-active,
  .route-fade-leave-active {
    transition: none;
  }

  .route-fade-enter-from,
  .route-fade-leave-to {
    transform: none;
  }
}
</style>
