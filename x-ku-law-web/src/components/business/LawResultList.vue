<template>
  <div class="results">
    <article v-for="(item, index) in items" :key="`${item.documentId}-${item.versionId}`" class="result" @click="$emit('open', item)">
      <div class="ordinal mono">{{ String(index + 1).padStart(2, '0') }}</div>
      <div class="result-main">
        <div class="result-title">
          <h3>{{ item.title }}</h3>
          <StatusBadge :value="item.status" />
        </div>
        <div class="meta">
          <span v-if="item.docNumber">{{ item.docNumber }}</span>
          <span v-if="item.effectLevel">{{ labelOf(item.effectLevel) }}</span>
          <span>{{ item.publishAuthority || '发布机关未标注' }}</span>
          <span>{{ item.effectiveDate || '生效日期未标注' }}</span>
        </div>
        <p v-if="highlightText(item)" v-html="highlightText(item)" />
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import StatusBadge from '@/components/common/StatusBadge.vue';
import type { LawSearchResult } from '@/types/law';
import { labelOf } from '@/utils/labels';

defineProps<{ items: LawSearchResult[] }>();
defineEmits<{ open: [item: LawSearchResult] }>();

function highlightText(item: LawSearchResult) {
  const values = Object.values(item.highlights || {}).flat();
  return values[0]?.replaceAll('<em>', '<span class="hl">').replaceAll('</em>', '</span>') || '';
}
</script>

<style scoped>
.results {
  border-top: 1px solid var(--ink);
}

.result {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 20px;
  padding: 24px 0;
  border-bottom: 1px solid var(--rule);
  cursor: pointer;
}

.result:hover {
  background: var(--paper-2);
}

.ordinal {
  color: var(--accent);
  padding-top: 4px;
}

.result-title {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

h3 {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 19px;
  font-weight: 400;
  letter-spacing: -0.005em;
  line-height: 1.2;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  margin-top: 8px;
  color: var(--muted);
  font-family: var(--mono);
  font-size: var(--font-xs);
}

p {
  max-width: 78ch;
  margin: 14px 0 0;
  color: var(--ink-2);
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.65;
}

@media (max-width: 720px) {
  .result {
    grid-template-columns: 1fr;
  }
}
</style>
