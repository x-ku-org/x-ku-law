<template>
  <header class="sk-law-meta" aria-hidden="true">
    <div class="sk-law-main">
      <span class="sk-kicker sk-shimmer" />
      <Skeleton width="72%" />
      <Skeleton width="48%" />
      <div class="sk-tags">
        <span v-for="n in 3" :key="n" class="sk-tag sk-shimmer" />
      </div>
    </div>
    <div class="sk-law-side">
      <div class="sk-actions">
        <span v-for="n in 4" :key="n" class="sk-action sk-shimmer" />
      </div>
      <div class="sk-stats">
        <Skeleton v-for="n in 4" :key="n" variant="stat" width="48px" />
      </div>
      <span class="sk-box sk-shimmer" />
    </div>
  </header>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-law-meta {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: var(--gutter);
  padding-bottom: 24px;
  border-bottom: 1px solid var(--rule);
}

.sk-law-main,
.sk-law-side {
  display: grid;
  gap: 14px;
}

.sk-kicker {
  width: 88px;
  height: 10px;
  border-radius: 2px;
}

.sk-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sk-tag {
  width: 64px;
  height: 24px;
  border-radius: 4px;
}

.sk-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sk-action {
  width: 72px;
  height: 32px;
  border-radius: 4px;
}

.sk-stats {
  display: flex;
  gap: 24px;
}

.sk-box {
  width: 100%;
  height: 120px;
  border-radius: 4px;
}

@media (max-width: 980px) {
  .sk-law-meta {
    grid-template-columns: 1fr;
  }
}
</style>
