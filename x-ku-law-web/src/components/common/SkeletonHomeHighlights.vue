<template>
  <section class="sk-highlights hairline-strong" aria-hidden="true">
    <div v-for="n in 4" :key="n" class="sk-highlight-cell" :class="{ 'has-rule': n < 4 }">
      <span class="sk-label sk-shimmer" />
      <Skeleton variant="stat" width="80px" />
      <span class="sk-caption sk-shimmer" />
    </div>
  </section>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-highlights {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding-bottom: 22px;
}

.sk-highlight-cell {
  display: grid;
  gap: 10px;
}

.sk-highlight-cell + .sk-highlight-cell {
  padding-left: 24px;
}

.sk-highlight-cell.has-rule {
  border-right: 1px solid var(--rule);
}

.sk-label {
  width: 72px;
  height: 10px;
  border-radius: 2px;
}

.sk-caption {
  width: 88px;
  height: 10px;
  margin-top: 6px;
  border-radius: 2px;
}

@media (max-width: 980px) {
  .sk-highlights {
    grid-template-columns: repeat(2, 1fr);
  }

  .sk-highlight-cell.has-rule {
    border-right: 0;
  }
}
</style>
