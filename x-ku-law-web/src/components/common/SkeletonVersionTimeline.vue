<template>
  <div class="sk-timeline" aria-hidden="true">
    <div v-for="n in count" :key="n" class="sk-version">
      <span class="sk-diamond sk-shimmer" />
      <div class="sk-version-text">
        <span class="sk-title sk-shimmer" />
        <span class="sk-date sk-shimmer" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 4 });
</script>

<style scoped>
.sk-timeline {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  align-items: stretch;
  padding: 18px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
}

.sk-version {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  min-width: 180px;
}

.sk-diamond {
  width: 9px;
  height: 9px;
  margin-top: 4px;
  border-radius: 1px;
  transform: rotate(45deg);
}

.sk-version-text {
  display: grid;
  gap: 8px;
  flex: 1;
}

.sk-title {
  width: 120px;
  height: 14px;
  border-radius: 2px;
}

.sk-date {
  width: 88px;
  height: 10px;
  border-radius: 2px;
}
</style>
