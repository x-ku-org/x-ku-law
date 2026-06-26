<template>
  <div class="sk-sessions">
    <div v-for="n in count" :key="n" class="sk-session">
      <span class="sk-session-title sk-shimmer" :style="{ width: titleWidth(n) }" />
      <span class="sk-session-time sk-shimmer" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 4 });

function titleWidth(n: number) {
  return ['72%', '58%', '66%', '50%'][(n - 1) % 4];
}
</script>

<style scoped>
.sk-sessions {
  display: grid;
  gap: 12px;
}

.sk-session {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

.sk-session-title {
  height: 12px;
  border-radius: 3px;
}

.sk-session-time {
  width: 38%;
  height: 9px;
  border-radius: 2px;
}
</style>
