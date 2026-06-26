<template>
  <div class="sk-pulse" aria-hidden="true">
    <div class="sk-pulse-head">
      <span class="sk-kicker sk-shimmer" />
      <span class="sk-link sk-shimmer" />
    </div>
    <div class="sk-pulse-body">
      <Skeleton variant="stat" width="64px" />
      <span class="sk-ring sk-shimmer" />
    </div>
    <span class="sk-note sk-shimmer" />
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-pulse {
  display: grid;
  gap: 16px;
}

.sk-pulse-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.sk-kicker {
  width: 56px;
  height: 10px;
  border-radius: 2px;
}

.sk-link {
  width: 72px;
  height: 10px;
  border-radius: 2px;
}

.sk-pulse-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sk-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  flex-shrink: 0;
}

.sk-note {
  width: 92%;
  height: 12px;
  border-radius: 2px;
}
</style>
