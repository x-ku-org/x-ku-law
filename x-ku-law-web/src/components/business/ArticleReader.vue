<template>
  <div class="reader">
    <article
      v-for="article in articles"
      :id="`article-${article.id}`"
      :key="article.id"
      class="article"
      :class="{ focused: activeArticleId === article.id }"
      @click="emit('select', article.id)"
    >
      <div class="article-head">
        <div class="article-no">
          <span>{{ article.articleNo || `第 ${article.articleOrder || article.id} 条` }}</span>
          <XChip v-if="article.obligationFlag" tone="accent">核心义务</XChip>
          <XChip v-if="article.penaltyFlag" tone="rose">责任条款</XChip>
        </div>
        <XButton
          v-if="activeArticleId === article.id"
          size="small"
          variant="ghost"
          class="article-copy"
          @click.stop="copyArticle(article)"
        >
          复制
        </XButton>
      </div>
      <h3 v-if="article.articleTitle">{{ article.articleTitle }}</h3>
      <p>{{ article.contentText || '该条款暂无正文。' }}</p>
    </article>
  </div>
</template>

<script setup lang="ts">
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import { useToast } from '@/composables/useToast';
import type { LawArticle } from '@/types/law';
import { copyText } from '@/utils/clipboard';

defineProps<{
  articles: LawArticle[];
  /** 目录点击或滚动视口时高亮当前条款。 */
  activeArticleId?: number | null;
}>();

const emit = defineEmits<{
  select: [articleId: number];
}>();

const toast = useToast();

function formatArticleText(article: LawArticle) {
  const no = article.articleNo || `第 ${article.articleOrder || article.id} 条`;
  const parts = [no];
  if (article.articleTitle) parts.push(article.articleTitle);
  if (article.contentText) parts.push(article.contentText);
  return parts.join('\n\n');
}

async function copyArticle(article: LawArticle) {
  const ok = await copyText(formatArticleText(article));
  if (ok) {
    toast.success('已复制条款内容。');
  } else {
    toast.error('复制失败，请手动选择文本。');
  }
}
</script>

<style scoped>
.reader {
  display: grid;
  gap: 6px;
}

.article {
  padding: 18px 22px 20px;
  border-left: 2px solid transparent;
  cursor: pointer;
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease);
}

.article.focused {
  border-left-color: var(--accent);
  background: var(--accent-soft);
}

.article-head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.article-copy {
  flex-shrink: 0;
  margin-top: 2px;
}

.article-no {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--accent);
  font-family: var(--serif-display);
  font-size: 23px;
  font-style: italic;
}

h3 {
  margin: 12px 0 0;
  font-family: var(--serif-body);
  font-size: 18px;
  font-weight: 500;
}

p {
  margin: 12px 0 0;
  color: var(--ink-2);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.78;
  text-align: justify;
  text-justify: inter-ideograph;
}
</style>
