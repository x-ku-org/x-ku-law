<template>
  <div class="sk-reading-grid" aria-hidden="true">
    <aside class="sk-toc">
      <span class="sk-kicker sk-shimmer" />
      <span v-for="n in 8" :key="n" class="sk-toc-link sk-shimmer" />
    </aside>
    <main class="sk-reading">
      <Skeleton v-for="n in 4" :key="n" width="100%" :lines="3" />
    </main>
    <aside class="sk-marginal">
      <span class="sk-kicker sk-shimmer" />
      <Skeleton variant="block" width="100%" />
      <Skeleton width="100%" :lines="2" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-reading-grid {
  display: grid;
  grid-template-columns: 200px 1fr 280px;
  gap: var(--gutter);
  align-items: start;
}

.sk-toc,
.sk-reading,
.sk-marginal {
  display: grid;
  gap: 12px;
}

.sk-kicker {
  width: 80px;
  height: 10px;
  border-radius: 2px;
}

.sk-toc-link {
  width: 90%;
  height: 12px;
  border-radius: 2px;
}

@media (max-width: 980px) {
  .sk-reading-grid {
    grid-template-columns: 1fr;
  }
}
</style>
