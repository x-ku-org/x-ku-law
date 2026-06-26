<template>
  <div class="sk-diff" aria-hidden="true">
    <Skeleton width="100%" />
    <div class="sk-chips">
      <span v-for="n in 4" :key="n" class="sk-chip sk-shimmer" />
    </div>
    <SkeletonList :count="4" />
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
</script>

<style scoped>
.sk-diff {
  display: grid;
  gap: 18px;
}

.sk-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sk-chip {
  width: 72px;
  height: 24px;
  border-radius: 4px;
}
</style>
