<template>
  <div class="sk-spotlight" aria-hidden="true">
    <span class="sk-kicker sk-shimmer" />
    <span class="sk-chip sk-shimmer" />
    <Skeleton width="72%" />
    <Skeleton width="90%" :lines="2" />
    <div class="sk-actions">
      <span class="sk-btn sk-shimmer" />
      <span class="sk-btn sk-shimmer sk-btn--ghost" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-spotlight {
  display: grid;
  gap: 16px;
  align-content: center;
  min-height: 380px;
  padding: 28px;
  border-radius: 4px;
  background: var(--ink);
}

.sk-kicker {
  width: 88px;
  height: 10px;
  border-radius: 2px;
  opacity: 0.55;
}

.sk-chip {
  width: 72px;
  height: 22px;
  border-radius: 4px;
}

.sk-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
}

.sk-btn {
  width: 88px;
  height: 32px;
  border-radius: 4px;
}

.sk-btn--ghost {
  width: 96px;
  opacity: 0.7;
}
</style>
