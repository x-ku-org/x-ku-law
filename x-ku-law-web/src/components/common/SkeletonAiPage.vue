<template>
  <div class="sk-ai-page" aria-hidden="true">
    <aside class="sk-sessions">
      <div class="sk-sessions-head">
        <span class="sk-kicker sk-shimmer" />
        <span class="sk-new sk-shimmer" />
      </div>
      <SkeletonSessions :count="4" />
    </aside>
    <main class="sk-opinion">
      <span class="sk-hero-kicker sk-shimmer" />
      <Skeleton width="62%" />
      <Skeleton width="48%" />
      <span class="sk-composer sk-shimmer" />
      <div class="sk-samples">
        <span v-for="n in 3" :key="n" class="sk-sample sk-shimmer" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
import SkeletonSessions from '@/components/common/SkeletonSessions.vue';
</script>

<style scoped>
.sk-ai-page {
  display: grid;
  grid-template-columns: 276px minmax(0, 1fr);
  gap: 0;
  min-height: 420px;
}

.sk-sessions {
  display: grid;
  gap: 12px;
  align-content: start;
  padding-right: 18px;
  border-right: 1px solid var(--rule);
}

.sk-sessions-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.sk-kicker {
  width: 48px;
  height: 10px;
  border-radius: 2px;
}

.sk-new {
  width: 88px;
  height: 28px;
  border-radius: 4px;
}

.sk-opinion {
  display: grid;
  gap: 16px;
  align-content: center;
  justify-items: center;
  padding: 24px 42px;
}

.sk-hero-kicker {
  width: 120px;
  height: 10px;
  border-radius: 2px;
}

.sk-composer {
  width: min(100%, 560px);
  height: 120px;
  border-radius: 4px;
}

.sk-samples {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.sk-sample {
  width: 140px;
  height: 32px;
  border-radius: 4px;
}

@media (max-width: 860px) {
  .sk-ai-page {
    grid-template-columns: 1fr;
  }

  .sk-sessions {
    padding-right: 0;
    border-right: 0;
    border-bottom: 1px solid var(--rule);
    padding-bottom: 16px;
  }
}
</style>
