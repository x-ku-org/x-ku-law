<template>
  <div class="pulse-card">
    <div class="pulse-head">
      <div class="section-kicker">§ Pulse</div>
      <RouterLink v-if="linkTo" :to="linkTo" class="pulse-link mono">查看全部 →</RouterLink>
    </div>
    <div class="pulse-body">
      <div class="pulse-score">
        <span class="num pulse-num">{{ score }}</span>
        <div class="pulse-meta">
          <span v-if="delta" class="mono pulse-delta" :class="deltaClass">{{ delta }}</span>
          <span class="mono pulse-sub">{{ subtitle }}</span>
        </div>
      </div>
      <svg class="pulse-ring" viewBox="0 0 80 80" aria-hidden="true">
        <circle cx="40" cy="40" r="34" fill="none" stroke="var(--paper-sunk)" stroke-width="6" />
        <circle
          cx="40"
          cy="40"
          r="34"
          fill="none"
          stroke="var(--accent)"
          stroke-width="6"
          stroke-linecap="round"
          :stroke-dasharray="ringDash"
          transform="rotate(-90 40 40)"
        />
      </svg>
    </div>
    <p v-if="note" class="pulse-note">{{ note }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, type RouteLocationRaw } from 'vue-router';

const props = withDefaults(
  defineProps<{
    score: number | string;
    percent?: number;
    delta?: string;
    subtitle?: string;
    note?: string;
    linkTo?: RouteLocationRaw;
  }>(),
  {
    percent: 78,
    delta: '',
    subtitle: '工作台活跃度',
    note: '',
    linkTo: undefined
  }
);

const ringDash = computed(() => {
  const pct = Math.min(100, Math.max(0, props.percent));
  const circumference = 2 * Math.PI * 34;
  const filled = (pct / 100) * circumference;
  return `${filled} ${circumference}`;
});

const deltaClass = computed(() => {
  if (!props.delta) return '';
  return props.delta.startsWith('+') || props.delta.startsWith('▲') ? 'up' : 'down';
});
</script>

<style scoped>
.pulse-card {
  display: grid;
  gap: 14px;
}

.pulse-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.pulse-link {
  font-size: var(--font-xs);
  color: var(--muted);
  text-decoration: none;
}

.pulse-link:hover {
  color: var(--accent);
}

.pulse-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid var(--rule);
  border-radius: 4px;
}

.pulse-score {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.pulse-num {
  font-size: 56px;
  line-height: 0.95;
  color: var(--ink);
}

.pulse-meta {
  display: grid;
  gap: 4px;
  padding-bottom: 8px;
}

.pulse-delta.up {
  color: var(--moss);
}

.pulse-delta.down {
  color: var(--rose);
}

.pulse-sub {
  color: var(--muted);
  font-size: var(--font-xs);
}

.pulse-ring {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
}

.pulse-note {
  margin: 0;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.5;
  color: var(--ink-3);
}
</style>
