<template>
  <section class="sk-matrix" aria-hidden="true">
    <div class="sk-matrix-head">
      <div class="sk-matrix-head-left">
        <span class="sk-kicker sk-shimmer" />
        <span class="sk-hint sk-shimmer" />
      </div>
      <div class="sk-toggle">
        <span v-for="n in 2" :key="n" class="sk-toggle-btn sk-shimmer" />
      </div>
    </div>

    <div class="sk-chart">
      <div class="sk-y-labels">
        <span v-for="n in rowCount" :key="n" class="sk-y-label sk-shimmer" />
      </div>
      <div class="sk-plot">
        <span v-for="n in rowCount" :key="`line-${n}`" class="sk-grid-line" />
        <span class="sk-axis" />
        <span
          v-for="(bubble, i) in bubbles"
          :key="`bubble-${i}`"
          class="sk-bubble sk-shimmer"
          :style="bubbleStyle(bubble)"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rowCount?: number }>(), { rowCount: 5 });

const bubbles = [
  { x: '18%', y: '12%', size: 10 },
  { x: '32%', y: '28%', size: 14 },
  { x: '48%', y: '8%', size: 8 },
  { x: '55%', y: '42%', size: 18 },
  { x: '68%', y: '22%', size: 12 },
  { x: '74%', y: '52%', size: 16 },
  { x: '82%', y: '36%', size: 9 },
  { x: '38%', y: '58%', size: 11 },
  { x: '62%', y: '68%', size: 13 }
];

function bubbleStyle(bubble: { x: string; y: string; size: number }) {
  return {
    left: bubble.x,
    top: bubble.y,
    width: `${bubble.size}px`,
    height: `${bubble.size}px`
  };
}
</script>

<style scoped>
.sk-matrix {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--rule);
}

.sk-matrix-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.sk-matrix-head-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sk-kicker {
  width: 88px;
  height: 10px;
  border-radius: 2px;
}

.sk-hint {
  width: 140px;
  height: 10px;
  border-radius: 2px;
}

.sk-toggle {
  display: inline-flex;
  gap: 6px;
}

.sk-toggle-btn {
  width: 48px;
  height: 24px;
  border-radius: 4px;
}

.sk-chart {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 12px;
  min-height: 158px;
  margin-top: 4px;
}

.sk-y-labels {
  display: grid;
  gap: 14px;
  padding-top: 6px;
}

.sk-y-label {
  width: 72px;
  height: 9px;
  margin-left: auto;
  border-radius: 2px;
}

.sk-plot {
  position: relative;
  min-height: 158px;
  border-left: 1px solid transparent;
}

.sk-grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  border-top: 1px dashed var(--rule);
  opacity: 0.78;
}

.sk-grid-line:nth-child(1) {
  top: 12%;
}

.sk-grid-line:nth-child(2) {
  top: 32%;
}

.sk-grid-line:nth-child(3) {
  top: 52%;
}

.sk-grid-line:nth-child(4) {
  top: 72%;
}

.sk-grid-line:nth-child(5) {
  top: 92%;
}

.sk-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8px;
  height: 1px;
  background: color-mix(in srgb, var(--ink) 55%, transparent);
}

.sk-bubble {
  position: absolute;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.85;
}
</style>
