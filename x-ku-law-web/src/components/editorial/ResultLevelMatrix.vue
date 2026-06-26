<template>
  <section class="matrix-section">
    <div class="matrix-head">
      <div class="matrix-head-left">
        <span class="section-kicker">§ 层级 × 时间</span>
        <span class="matrix-hint">{{ hint || activeHint }}</span>
      </div>
      <div class="matrix-toggle" role="tablist" aria-label="分布图样式">
        <button
          v-for="opt in modes"
          :key="opt.value"
          type="button"
          role="tab"
          :aria-selected="mode === opt.value"
          class="matrix-toggle-btn"
          :class="{ active: mode === opt.value }"
          @click="mode = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <svg
      v-if="cells.length"
      class="matrix"
      :class="`matrix--${mode}`"
      :viewBox="`0 0 ${width} ${height}`"
      role="img"
      aria-label="效力层级与时间分布"
    >
      <line
        v-if="mode === 'bubble'"
        :x1="padL"
        :y1="height - padB + 3"
        :x2="width - padR"
        :y2="height - padB + 3"
        class="matrix-axis"
      />
      <g v-for="(level, li) in levels" :key="`row-${level}`">
        <line
          v-if="mode === 'bubble'"
          :x1="padL"
          :y1="rowY(li) + cellH / 2"
          :x2="width - padR"
          :y2="rowY(li) + cellH / 2"
          class="matrix-row-line"
        />
        <text :x="padL - 10" :y="rowY(li) + cellH / 2 + 3" text-anchor="end" class="matrix-label">{{ level }}</text>
      </g>
      <g v-for="(year, yi) in years" :key="`col-${year}`">
        <text :x="colX(yi) + cellW / 2" :y="height - 8" text-anchor="middle" class="matrix-label">{{ year }}</text>
      </g>

      <template v-if="mode === 'heat'">
        <g v-for="cell in cells" :key="cell.id">
          <rect
            :x="colX(cell.colIndex) + gap / 2"
            :y="rowY(cell.levelIndex) + gap / 2"
            :width="cellW - gap"
            :height="cellH - gap"
            rx="3"
            :fill="cell.count ? 'var(--accent)' : 'var(--paper-sunk)'"
            :fill-opacity="cell.count ? 0.1 + cell.intensity * 0.9 : 1"
            class="matrix-cell"
            @click="emit('nodeClick', cell)"
          >
            <title>{{ cell.level }} · {{ cell.year }}：{{ cell.count }} 件</title>
          </rect>
          <text
            v-if="cell.count && cellW - gap >= 24"
            :x="colX(cell.colIndex) + cellW / 2"
            :y="rowY(cell.levelIndex) + cellH / 2 + 3"
            text-anchor="middle"
            class="matrix-count"
            :class="{ 'on-dark': cell.intensity > 0.55 }"
          >
            {{ cell.count }}
          </text>
        </g>
      </template>

      <template v-else>
        <g
          v-for="cell in cells"
          :key="cell.id"
          class="matrix-bubble"
          :class="{ empty: !cell.count }"
          @click="emit('nodeClick', cell)"
        >
          <circle
            :cx="colX(cell.colIndex) + cellW / 2"
            :cy="rowY(cell.levelIndex) + cellH / 2"
            :r="cell.count ? cell.radius + 4 : 0"
            class="matrix-bubble-ring"
          />
          <circle
            :cx="colX(cell.colIndex) + cellW / 2"
            :cy="rowY(cell.levelIndex) + cellH / 2"
            :r="cell.count ? cell.radius : 2"
            :fill-opacity="cell.count ? 0.56 + cell.intensity * 0.38 : 0.52"
            class="matrix-cell"
          >
            <title>{{ cell.level }} · {{ cell.year }}：{{ cell.count }} 件</title>
          </circle>
        </g>
      </template>
    </svg>
    <p v-else class="matrix-empty mono">当前结果不足以绘制分布矩阵</p>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

export interface MatrixNode {
  id: string;
  levelIndex: number;
  year: number;
  count: number;
}

interface MatrixCell extends MatrixNode {
  colIndex: number;
  level: string;
  intensity: number;
  radius: number;
}

type MatrixMode = 'bubble' | 'heat';

const props = defineProps<{
  levels: string[];
  years: number[];
  nodes: MatrixNode[];
  hint?: string;
}>();

const emit = defineEmits<{
  nodeClick: [node: MatrixNode];
}>();

const modes: { value: MatrixMode; label: string }[] = [
  { value: 'bubble', label: '气泡' },
  { value: 'heat', label: '热力' }
];
const mode = ref<MatrixMode>('bubble');
const activeHint = computed(() =>
  mode.value === 'bubble' ? '气泡半径表示命中密度' : '颜色深浅表示命中密度'
);

const width = 1180;
const padL = 104;
const padR = 26;
const padT = 8;
const padB = 30;
const cellH = 24;
const gap = 4;

const height = computed(() => padT + props.levels.length * cellH + padB);
const cellW = computed(() => (width - padL - padR) / Math.max(props.years.length, 1));
const minBubbleRadius = 4;
const bubbleSteps = 8;
const maxRadius = computed(() => Math.max(12, Math.min(cellW.value / 2 - 4, 14)));

const yearIndex = computed(() => {
  const map = new Map<number, number>();
  props.years.forEach((year, i) => map.set(year, i));
  return map;
});

const cells = computed<MatrixCell[]>(() => {
  const max = props.nodes.reduce((m, n) => Math.max(m, n.count), 0);
  return props.nodes
    .map((node) => {
      const colIndex = yearIndex.value.get(node.year);
      if (colIndex === undefined) return null;
      const intensity = max && node.count ? Math.pow(node.count / max, 0.72) : 0;
      const radiusStep = node.count ? Math.max(1, Math.ceil(intensity * bubbleSteps)) / bubbleSteps : 0;
      return {
        ...node,
        colIndex,
        level: props.levels[node.levelIndex] ?? '',
        intensity,
        radius: node.count ? minBubbleRadius + (maxRadius.value - minBubbleRadius) * radiusStep : 0
      } as MatrixCell;
    })
    .filter((c): c is MatrixCell => c !== null);
});

function rowY(index: number) {
  return padT + index * cellH;
}

function colX(index: number) {
  return padL + index * cellW.value;
}
</script>

<style scoped>
.matrix-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--rule);
}

.matrix-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.matrix-head-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.matrix-hint {
  font-family: var(--sans);
  font-size: var(--font-xs);
  color: var(--muted);
}

.matrix-toggle {
  display: inline-flex;
  gap: 6px;
}

.matrix-toggle-btn {
  height: 24px;
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: transparent;
  color: var(--ink-2);
  font-family: var(--sans);
  font-size: var(--font-xs);
  cursor: pointer;
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease), color 0.15s var(--ease);
}

.matrix-toggle-btn:hover {
  border-color: var(--ink);
}

.matrix-toggle-btn.active {
  border-color: var(--accent);
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.matrix {
  width: 100%;
  max-width: none;
  height: auto;
  overflow: visible;
}

.matrix--bubble {
  display: block;
  margin-top: 4px;
}

.matrix-label {
  font-family: var(--sans);
  font-size: 9px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  fill: var(--muted);
}

.matrix-row-line {
  stroke: var(--rule);
  stroke-width: 1;
  stroke-dasharray: 2 8;
  stroke-linecap: round;
  opacity: 0.78;
}

.matrix-axis {
  stroke: var(--ink);
  stroke-width: 1;
  opacity: 0.55;
}

.matrix-bubble {
  cursor: pointer;
}

.matrix-bubble.empty {
  pointer-events: none;
}

.matrix-bubble-ring {
  fill: var(--accent-glow);
  opacity: 0;
  transition: opacity 0.15s var(--ease);
}

.matrix-cell {
  cursor: pointer;
  transition: fill-opacity 0.15s var(--ease), stroke-opacity 0.15s var(--ease);
}

.matrix-bubble .matrix-cell {
  fill: var(--accent);
  stroke: color-mix(in srgb, var(--accent) 68%, var(--paper));
  stroke-width: 1;
}

.matrix-bubble.empty .matrix-cell {
  fill: var(--paper-sunk);
  stroke: var(--rule-strong);
  stroke-width: 1;
}

.matrix-cell:hover {
  fill-opacity: 1 !important;
}

.matrix-bubble:hover .matrix-bubble-ring {
  opacity: 0.75;
}

.matrix-bubble:hover .matrix-cell {
  fill-opacity: 1 !important;
  stroke-opacity: 1;
}

.matrix-count {
  font-family: var(--mono, monospace);
  font-size: 9px;
  fill: var(--ink);
  pointer-events: none;
}

.matrix-count.on-dark {
  fill: var(--paper);
}

.matrix-empty {
  margin: 0;
  padding: 24px 0;
  color: var(--muted);
  font-size: 12px;
}
</style>
