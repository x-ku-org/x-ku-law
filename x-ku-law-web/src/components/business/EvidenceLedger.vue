<template>
  <aside class="ledger">
    <div class="ledger-head">
      <div class="ledger-titles">
        <div class="section-kicker">§ 证据</div>
        <h2>证据账册</h2>
      </div>
      <button type="button" class="ledger-close" title="收起" @click="emit('close')">✕</button>
    </div>

    <div ref="scrollEl" class="ledger-scroll">
      <template v-if="groups.length">
        <section v-for="group in groups" :key="group.messageId" class="cite-group">
          <div class="group-label mono">{{ group.label }}</div>
          <button
            v-for="cite in group.citations"
            :key="keyOf(group.messageId, cite)"
            :ref="(el) => registerCard(keyOf(group.messageId, cite), el)"
            type="button"
            class="cite-card"
            :class="{ active: keyOf(group.messageId, cite) === activeKey }"
            @click="emit('update:activeKey', keyOf(group.messageId, cite))"
          >
            <div class="cite-head">
              <span class="cite-num mono">{{ numOf(cite.id) }}</span>
              <strong class="cite-source">{{ cite.source || cite.article || '法条引用' }}</strong>
              <span
                v-if="validityLabel(cite.validityStatus)"
                class="cite-validity"
                :class="`cite-validity--${cite.validityStatus}`"
                :title="validityLabel(cite.validityStatus)"
              >{{ validityLabel(cite.validityStatus) }}</span>
            </div>
            <div v-if="cite.article" class="cite-article mono">{{ cite.article }}</div>
            <p
              v-if="cite.excerpt"
              class="cite-excerpt"
              :class="{ 'cite-excerpt--full': keyOf(group.messageId, cite) === activeKey }"
            >
              {{ cite.excerpt }}
            </p>
            <XButton size="small" variant="ghost" @click.stop="emit('openLaw', cite)">跳转正文 →</XButton>
          </button>
        </section>
      </template>

      <template v-else>
        <EmptyState title="暂无引用证据" description="当前会话尚未生成带引用的回答。" />
      </template>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';
import type { ComponentPublicInstance } from 'vue';
import EmptyState from '@/components/common/EmptyState.vue';
import XButton from '@/components/common/XButton.vue';
import type { AiCitationGroup, AiMessageCitation } from '@/types/workspace';

const props = withDefaults(
  defineProps<{
    groups?: AiCitationGroup[];
    activeKey?: string | null;
  }>(),
  {
    groups: () => [],
    activeKey: null
  }
);

const emit = defineEmits<{
  'update:activeKey': [key: string];
  openLaw: [cite: AiMessageCitation];
  close: [];
}>();

// 复合键：同一 [n] 在不同回答里指向不同条款，故按 messageId 隔离。
function keyOf(messageId: number, cite: AiMessageCitation) {
  return `${messageId}#${cite.id}`;
}

function numOf(id: string) {
  return id.replace(/[[\]]/g, '');
}

function validityLabel(status?: string) {
  if (status === 'repealed') return '已废止';
  if (status === 'superseded') return '历史版本';
  return '';
}

const scrollEl = ref<HTMLElement | null>(null);
const cards = new Map<string, HTMLElement>();
function registerCard(key: string, el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) cards.set(key, el);
  else cards.delete(key);
}

// immediate：账册由关闭→打开时是全新挂载，此时 activeKey 往往已被点击赋值，
watch(
  () => props.activeKey,
  async (key) => {
    if (!key) return;
    await nextTick();
    const card = cards.get(key);
    const cont = scrollEl.value;
    if (!card || !cont) return;
    const delta = card.getBoundingClientRect().top - cont.getBoundingClientRect().top;
    cont.scrollTo({ top: cont.scrollTop + delta - 12, behavior: 'smooth' });
  },
  { immediate: true }
);
</script>

<style scoped>
.ledger {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 348px;
  height: 100%;
  min-height: 0;
  padding: 24px;
  border-left: 1px solid var(--rule);
  background: var(--paper-2);
}

.ledger-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.ledger-titles {
  display: grid;
  gap: 8px;
}

.ledger-close {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
  color: var(--muted);
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}

.ledger-close:hover {
  border-color: var(--accent);
  color: var(--accent-deep);
}

h2 {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 30px;
  font-weight: 400;
}

.ledger-scroll {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 12px;
  align-content: start;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.ledger-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.note {
  margin: 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  line-height: 1.65;
}

.cite-group {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 8px;
}

.group-label {
  font-size: var(--font-xxs);
  letter-spacing: 0.06em;
  color: var(--muted-2);
  text-transform: uppercase;
}

.cite-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 5px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-card);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s var(--ease), background 0.2s var(--ease);
}

.cite-card.active {
  border-color: var(--accent);
  background: var(--accent-soft);
}

.cite-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cite-num {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--paper-sunk);
  color: var(--ink-2);
  font-size: 10px;
}

.cite-card.active .cite-num {
  background: var(--accent);
  color: #fff;
}

.cite-source {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  font-family: var(--serif-body);
  font-size: 13px;
  font-weight: 500;
  color: var(--ink);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cite-validity {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  line-height: 1.5;
  white-space: nowrap;
}

.cite-validity--repealed {
  background: var(--danger-soft, #fbeaea);
  color: var(--danger, #b42318);
}

.cite-validity--superseded {
  background: var(--paper-sunk);
  color: var(--muted);
}

.cite-article {
  color: var(--muted);
  font-size: var(--font-xxs);
}

.cite-excerpt {
  margin: 2px 0;
  font-family: var(--serif-body);
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--ink-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cite-excerpt--full {
  padding-left: 10px;
  border-left: 2px solid var(--accent-glow);
  overflow: visible;
  -webkit-line-clamp: unset;
}

@media (max-width: 1120px) {
  .ledger {
    width: auto;
  }
}
</style>
