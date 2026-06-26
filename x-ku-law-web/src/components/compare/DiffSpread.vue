<template>
  <section class="diff-spread">
    <div class="pane pane--old">
      <p v-if="leftDate" class="pane-date mono">{{ leftDate }}</p>
      <p v-if="leftLabel" class="pane-label mono">{{ leftLabel }}</p>
      <div class="diff-text body-serif">
        <template v-if="changeType === 'ADDED'">
          <span class="diff-empty">—</span>
        </template>
        <template v-else>
          <span
            v-for="(seg, i) in leftSegments"
            :key="`l-${i}`"
            :class="segClass(seg.type, 'left')"
          >{{ seg.text }}</span>
        </template>
      </div>
    </div>

    <div class="gutter">
      <span class="mono gutter-no">{{ articleNo || '—' }}</span>
      <Diamond :size="10" />
      <XChip :tone="chipTone">{{ changeLabel }}</XChip>
      <p v-if="note" class="gutter-note">{{ note }}</p>
    </div>

    <div class="pane pane--new">
      <p v-if="rightDate" class="pane-date mono">{{ rightDate }}</p>
      <p v-if="rightLabel" class="pane-label mono">{{ rightLabel }}</p>
      <div class="diff-text body-serif">
        <template v-if="changeType === 'REMOVED'">
          <span class="diff-empty">—</span>
        </template>
        <template v-else>
          <span
            v-for="(seg, i) in rightSegments"
            :key="`r-${i}`"
            :class="segClass(seg.type, 'right')"
          >{{ seg.text }}</span>
        </template>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Diamond from '@/components/brand/Diamond.vue';
import XChip from '@/components/common/XChip.vue';
import { diffSegments, type DiffSegment } from '@/utils/textDiff';
import type { ArticleChangeType } from '@/types/law';

const props = defineProps<{
  baseText?: string;
  targetText?: string;
  changeType: ArticleChangeType;
  articleNo?: string;
  note?: string;
  leftLabel?: string;
  rightLabel?: string;
  leftDate?: string;
  rightDate?: string;
}>();

const segments = computed(() => diffSegments(props.baseText || '', props.targetText || ''));

const leftSegments = computed(() =>
  props.changeType === 'ADDED'
    ? []
    : segments.value.filter((s) => s.type !== 'add')
);

const rightSegments = computed(() =>
  props.changeType === 'REMOVED'
    ? []
    : segments.value.filter((s) => s.type !== 'del')
);

const changeLabel = computed(() => {
  const map: Record<ArticleChangeType, string> = {
    ADDED: '新增',
    REMOVED: '删除',
    MODIFIED: '修订',
    UNCHANGED: '未变'
  };
  return map[props.changeType] || '变更';
});

const chipTone = computed((): 'moss' | 'gold' | 'rose' | 'outline' => {
  if (props.changeType === 'ADDED') return 'moss';
  if (props.changeType === 'REMOVED') return 'rose';
  if (props.changeType === 'MODIFIED') return 'gold';
  return 'outline';
});

function segClass(type: DiffSegment['type'], side: 'left' | 'right') {
  if (type === 'same') return '';
  if (type === 'del' && side === 'left') return 'diff-del';
  if (type === 'add' && side === 'right') return 'diff-add';
  if (type === 'del' && side === 'right') return '';
  if (type === 'add' && side === 'left') return '';
  return side === 'left' ? 'diff-del' : 'diff-add';
}
</script>

<style scoped>
.diff-spread {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px minmax(0, 1fr);
  gap: 0;
  border: 1px solid var(--rule);
  border-radius: 4px;
  overflow: hidden;
}

.pane {
  display: grid;
  gap: 12px;
  align-content: start;
  padding: 22px 24px;
}

.pane--old {
  border-right: 1px solid var(--rule);
}

.pane--new {
  border-left: 1px solid var(--rule);
}

.pane-date {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--accent-deep);
  letter-spacing: 0.02em;
}

.pane-label {
  margin: 0;
  font-size: var(--font-xs);
  color: var(--muted);
}

.diff-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.diff-empty {
  color: var(--muted-2);
}

.diff-add {
  background: var(--moss-soft);
}

.diff-del {
  background: var(--rose-soft);
  text-decoration: line-through;
}

.gutter {
  display: grid;
  gap: 10px;
  place-items: center;
  align-content: center;
  padding: 16px 8px;
  background: var(--paper-2);
}

.gutter-no {
  font-size: var(--font-xs);
  color: var(--ink-2);
}

.gutter-note {
  margin: 0;
  font-size: var(--font-xs);
  line-height: 1.4;
  text-align: center;
  color: var(--muted);
}

@media (max-width: 860px) {
  .diff-spread {
    grid-template-columns: 1fr;
  }

  .pane--old,
  .pane--new {
    border: 0;
    border-bottom: 1px solid var(--rule);
  }
}
</style>
