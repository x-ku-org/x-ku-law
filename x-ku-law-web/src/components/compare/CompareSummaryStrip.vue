<template>
  <section class="summary-strip">
    <div v-for="stat in stats" :key="stat.label" class="stat">
      <div class="section-kicker">{{ stat.kicker }}</div>
      <span class="num stat-num" :style="stat.color ? { color: stat.color } : undefined">{{ stat.value }}</span>
      <span class="mono stat-sub">{{ stat.sub }}</span>
    </div>
  </section>
</template>

<script setup lang="ts">
export interface CompareStat {
  kicker: string;
  label: string;
  value: number | string;
  sub: string;
  color?: string;
}

defineProps<{
  stats: CompareStat[];
}>();
</script>

<style scoped>
.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid var(--rule);
  border-radius: 4px;
  overflow: hidden;
}

.stat {
  display: grid;
  gap: 10px;
  padding: 24px 22px;
  border-right: 1px solid var(--rule);
}

.stat:last-child {
  border-right: 0;
}

.stat-num {
  font-size: 52px;
  line-height: 0.95;
}

.stat-sub {
  color: var(--muted);
  font-size: var(--font-xs);
}

@media (max-width: 860px) {
  .summary-strip {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat:nth-child(2) {
    border-right: 0;
  }
}
</style>
