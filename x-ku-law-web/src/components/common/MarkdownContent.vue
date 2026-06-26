<template>
  <div class="markdown-content" :class="toneClass" v-html="html" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { renderMarkdown } from '@/utils/markdown';

const props = withDefaults(
  defineProps<{
    source?: string;
    /** light：正文区；inset：深色 AI 卡内。 */
    tone?: 'light' | 'inset';
  }>(),
  {
    source: '',
    tone: 'light'
  }
);

const html = computed(() => renderMarkdown(props.source || ''));
const toneClass = computed(() => `markdown-content--${props.tone}`);
</script>

<style scoped>
.markdown-content {
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.75;
  color: var(--ink-2);
}

.markdown-content--inset {
  font-size: 14px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.92);
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4) {
  margin: 1.1em 0 0.45em;
  font-family: var(--serif-body);
  font-weight: 600;
  color: inherit;
  font-style: normal;
}

.markdown-content :deep(h2) {
  font-size: 1.05em;
}

.markdown-content :deep(h3) {
  font-size: 1em;
}

.markdown-content :deep(p) {
  margin: 0.55em 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.25em;
}

.markdown-content :deep(li) {
  margin: 0.25em 0;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.markdown-content :deep(a) {
  color: var(--accent);
  text-decoration: underline;
  text-underline-offset: 2px;
}

.markdown-content--inset :deep(a) {
  color: #9eb8ff;
}

.markdown-content :deep(code) {
  font-family: var(--mono);
  font-size: 0.92em;
}

.markdown-content--inset :deep(h2),
.markdown-content--inset :deep(h3) {
  color: rgba(255, 255, 255, 0.98);
}
</style>
