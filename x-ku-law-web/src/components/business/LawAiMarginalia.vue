<template>
  <div class="ai-marginalia" :class="{ 'ai-marginalia--empty': !hasContent }">
    <div v-if="hasContent" class="ai-card">
      <header class="ai-card-head">
        <Diamond :size="7" />
        <span>X-KU 智询解读</span>
      </header>
      <p class="ai-card-excerpt">{{ previewText }}</p>
      <button type="button" class="ai-expand" @click="emit('expand')">展开 →</button>
    </div>
    <p v-else class="ai-empty">暂无解读。</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Diamond from '@/components/brand/Diamond.vue';
import { excerptOpening } from '@/utils/interpretationExcerpt';

const props = defineProps<{
  interpretationText?: string;
}>();

const emit = defineEmits<{ expand: [] }>();

const hasContent = computed(() => Boolean(props.interpretationText?.trim()));

const previewText = computed(() => excerptOpening(props.interpretationText?.trim() || ''));
</script>

<style scoped>
.ai-marginalia {
  display: grid;
  gap: 10px;
}

.ai-card {
  position: relative;
  overflow: hidden;
  padding: 16px 16px 14px;
  border-radius: 4px;
  background: var(--ink);
  color: var(--paper);
}

.ai-card-head {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
  color: #9eb8ff;
  font-family: var(--sans);
  font-size: var(--font-xs);
  font-weight: 600;
  letter-spacing: 0.06em;
}

.ai-card-excerpt {
  margin: 0;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.9);
}

.ai-expand {
  margin-top: 14px;
  padding: 0;
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.78);
  font-family: var(--sans);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s var(--ease);
}

.ai-expand:hover {
  color: var(--paper);
}

.ai-empty {
  margin: 0;
  padding: 14px;
  border: 1px dashed var(--rule-strong);
  border-radius: 4px;
  color: var(--muted);
  font-family: var(--serif-body);
  font-size: 13px;
  line-height: 1.55;
}
</style>
