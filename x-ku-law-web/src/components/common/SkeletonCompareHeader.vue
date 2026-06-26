<template>
  <header class="sk-compare-head" aria-hidden="true">
    <span class="sk-kicker sk-shimmer" />
    <Skeleton width="55%" />
    <Skeleton width="72%" :lines="2" />
  </header>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-compare-head {
  display: grid;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--ink);
}

.sk-kicker {
  width: 88px;
  height: 10px;
  border-radius: 2px;
}
</style>
