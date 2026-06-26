<template>
  <div ref="root" class="opinion-layout">
    <Transition name="opinion-title">
      <h2 v-if="title" class="opinion-title display">{{ title }}</h2>
    </Transition>

    <TransitionGroup name="message" tag="div" class="message-list">
      <article
        v-for="(message, index) in messages"
        :key="message.clientKey ?? message.id"
        class="message-block"
        :class="message.role"
      >
        <div class="message-role-line">
          <span class="mono message-role">{{ roleLabel(message.role) }}</span>
          <Transition name="answer-now">
            <XButton
              v-if="showAnswerNow && streaming && message.role === 'assistant' && index === messages.length - 1"
              size="small"
              variant="ghost"
              class="answer-now-btn"
              @click="emit('answerNow')"
            >
              立即作答
            </XButton>
          </Transition>
        </div>

        <template v-if="isStructured(message)">
          <blockquote v-if="tldrText(message)" class="tldr marginalia">{{ tldrText(message) }}</blockquote>
          <ol v-if="steps(message).length" class="steps">
            <li v-for="step in steps(message)" :key="step.n || step.title" class="step">
              <span class="mono step-no">{{ step.n }}</span>
              <div>
                <strong v-if="step.title">{{ step.title }}</strong>
                <p>
                  {{ step.body }}
                  <button
                    v-for="citeId in step.citeIds || []"
                    :key="citeId"
                    type="button"
                    class="cite-ref"
                    :class="{ 'cite-ref--active': activeCiteKey === `${message.id}#${citeId}` }"
                    @click="emit('citeClick', { messageId: message.id, citeId })"
                  >
                    {{ stripBrackets(citeId) }}
                  </button>
                </p>
              </div>
            </li>
          </ol>
        </template>

        <blockquote v-else-if="message.role === 'user'" class="user-quote">{{ message.content }}</blockquote>
        <template v-else-if="message.content">
          <div
            v-if="message.role === 'assistant' && message.riskLevel === 'warning'"
            class="risk-banner"
          >
            <span class="risk-icon" aria-hidden="true">!</span>
            <span>本回答依据可能存在不确定性（命中失效/废止条款、低置信或缺少有效引用），请结合现行有效法规审慎参考。</span>
          </div>
          <div
            class="assistant-body body-serif markdown-body"
            @click="onCiteClick"
            v-html="messageHtml(message)"
          />
        </template>
        <div
          v-else-if="streaming && index === messages.length - 1"
          class="assistant-body body-serif assistant-pending"
        >
          <span class="pending-text">{{ stageHint ? `正在${stageHint}` : '正在生成回答' }}</span>
          <span class="typing-dots" aria-hidden="true"><i></i><i></i><i></i></span>
        </div>
        <p v-else class="assistant-body body-serif assistant-empty">该消息暂无正文。</p>

        <div
          v-if="
            message.role === 'assistant' &&
              answerText(message) &&
              !(streaming && index === messages.length - 1)
          "
          class="answer-actions"
        >
          <button
            type="button"
            class="answer-action mono"
            :class="{ 'answer-action--done': message.liked }"
            title="这条回答有帮助"
            @click="emit('toggleLike', message.id)"
          >
            {{ message.liked ? '已赞 ✓' : '有帮助' }}
          </button>
          <button
            type="button"
            class="answer-action mono"
            title="纠错"
            @click="openErrorFeedback(message.id)"
          >
            纠错
          </button>
          <button type="button" class="answer-action mono" @click="copyAnswer(message)">
            {{ copiedId === message.id ? '已复制 ✓' : '复制' }}
          </button>
          <button
            type="button"
            class="answer-action mono"
            :disabled="downloadingId === message.id"
            @click="downloadAnswer(message, index)"
          >
            {{ downloadingId === message.id ? '生成中…' : '下载 PDF' }}
          </button>
        </div>
      </article>
    </TransitionGroup>

    <XModal
      :open="errorModalOpen"
      title="纠错反馈"
      kicker="§ Feedback"
      max-width="520px"
      @update:open="errorModalOpen = $event"
    >
      <div class="feedback-modal">
        <p class="feedback-tip">请简要描述这条回答的问题或正确表述，便于我们核查改进（可留空直接提交）。</p>
        <XTextarea
          v-model="errorText"
          :rows="4"
          placeholder="例如：引用的条款已废止 / 赔偿倍数表述有误…"
        />
      </div>
      <template #footer>
        <XButton variant="ghost" @click="errorModalOpen = false">取消</XButton>
        <XButton variant="primary" @click="submitErrorFeedback">提交反馈</XButton>
      </template>
    </XModal>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUpdated, ref, watch } from 'vue';
import type { AiMessage } from '@/types/workspace';
import { renderMarkdown } from '@/utils/markdown';
import XButton from '@/components/common/XButton.vue';
import XModal from '@/components/common/XModal.vue';
import XTextarea from '@/components/common/XTextarea.vue';

const props = defineProps<{
  messages: AiMessage[];
  title?: string;
  streaming?: boolean;
  stageHint?: string;
  showAnswerNow?: boolean;
  activeCiteKey?: string | null;
  userLabel?: string;
}>();

function stripBrackets(citeId: string) {
  return citeId.replace(/[[\]]/g, '');
}

function decorateCitations(html: string, messageId: number): string {
  return html.replace(/(<[^>]+>)|\[(\d{1,3})\]/g, (match, tag, n) =>
    tag ? tag : `<sup class="cite-ref" data-msg="${messageId}" data-cite="[${n}]">${n}</sup>`
  );
}

// 选中项变化时，给正文中所有匹配的角标加 active 类（v-html 渲染的节点无法用模板 :class，故用 DOM 同步）。
const root = ref<HTMLElement | null>(null);
function applyActiveHighlight() {
  const el = root.value;
  if (!el) return;
  el.querySelectorAll('.markdown-body .cite-ref.cite-ref--active').forEach((n) => n.classList.remove('cite-ref--active'));
  const key = props.activeCiteKey;
  if (!key) return;
  const hash = key.indexOf('#');
  if (hash < 0) return;
  const msg = key.slice(0, hash);
  const cite = key.slice(hash + 1);
  el.querySelectorAll(`.markdown-body .cite-ref[data-msg="${msg}"][data-cite="${cite}"]`).forEach((n) =>
    n.classList.add('cite-ref--active')
  );
}
onMounted(applyActiveHighlight);
onUpdated(applyActiveHighlight); // v-html 重渲染后重新贴 active 类
watch(
  () => props.activeCiteKey,
  () => nextTick(applyActiveHighlight)
);

const htmlCache = new Map<number, { content: string; html: string }>();
function messageHtml(message: AiMessage): string {
  const content = message.content ?? '';
  const cached = htmlCache.get(message.id);
  if (cached && cached.content === content) return cached.html;
  const html = decorateCitations(renderMarkdown(content), message.id);
  htmlCache.set(message.id, { content, html });
  return html;
}

const emit = defineEmits<{
  citeClick: [payload: { messageId: number; citeId: string }];
  answerNow: [];
  toggleLike: [messageId: number];
  feedback: [payload: { messageId: number; feedbackType: string; content?: string }];
}>();

const errorModalOpen = ref(false);
const errorTargetId = ref<number | null>(null);
const errorText = ref('');
function openErrorFeedback(messageId: number) {
  errorTargetId.value = messageId;
  errorText.value = '';
  errorModalOpen.value = true;
}
function submitErrorFeedback() {
  const messageId = errorTargetId.value;
  if (messageId == null) return;
  emit('feedback', { messageId, feedbackType: 'error', content: errorText.value.trim() || undefined });
  errorModalOpen.value = false;
  errorTargetId.value = null;
  errorText.value = '';
}

function onCiteClick(event: MouseEvent) {
  const target = (event.target as HTMLElement | null)?.closest('.cite-ref') as HTMLElement | null;
  if (!target) return;
  const messageId = Number(target.dataset.msg);
  const citeId = target.dataset.cite;
  if (!Number.isNaN(messageId) && citeId) emit('citeClick', { messageId, citeId });
}

function roleLabel(role?: string) {
  if (role === 'user') return props.userLabel || '用户';
  if (role === 'assistant') return 'X-KU 智询助手';
  return (role || 'message').toUpperCase();
}

function isStructured(message: AiMessage) {
  return Boolean(message.blocks?.length);
}

function tldrText(message: AiMessage) {
  const block = message.blocks?.find((b) => b.kind === 'tldr');
  return block?.body || '';
}

function steps(message: AiMessage) {
  return (message.blocks || []).filter((b) => b.kind === 'step');
}


function answerText(message: AiMessage): string {
  if (message.content?.trim()) return message.content.trim();
  if (!message.blocks?.length) return '';
  const parts: string[] = [];
  for (const b of message.blocks) {
    if (b.kind === 'tldr' && b.body) parts.push(`> ${b.body}`);
    else if (b.kind === 'step') {
      const head = [b.n, b.title].filter(Boolean).join('. ');
      parts.push([head && `### ${head}`, b.body].filter(Boolean).join('\n'));
    } else if (b.body) parts.push(b.body);
  }
  return parts.join('\n\n').trim();
}

function answerTitle(index: number): string {
  if (props.title?.trim()) return props.title.trim();
  const prev = props.messages[index - 1];
  if (prev?.role === 'user' && prev.content?.trim()) return prev.content.trim().slice(0, 40);
  return '法律意见';
}

const copiedId = ref<number | null>(null);
let copiedTimer: ReturnType<typeof setTimeout> | null = null;
async function copyAnswer(message: AiMessage) {
  const text = answerText(message);
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
  } catch {
    // 退回 execCommand：剪贴板 API 在非安全上下文不可用时兜底。
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.select();
    try {
      document.execCommand('copy');
    } finally {
      document.body.removeChild(ta);
    }
  }
  copiedId.value = message.id;
  if (copiedTimer) clearTimeout(copiedTimer);
  copiedTimer = setTimeout(() => (copiedId.value = null), 1600);
}

const downloadingId = ref<number | null>(null);
async function downloadAnswer(message: AiMessage, index: number) {
  const text = answerText(message);
  if (!text || downloadingId.value === message.id) return;
  downloadingId.value = message.id;
  try {
    const { downloadInterpretationPdf } = await import('@/utils/interpretationExport');
    await downloadInterpretationPdf(answerTitle(index), text, {
      subtitle: 'X-KU 智询助手 · AI 回答',
      filenameSuffix: 'AI回答',
      appendix: (message.citations || []).map((c) => ({
        id: c.id,
        source: c.source,
        article: c.article,
        excerpt: c.excerpt
      }))
    });
  } finally {
    downloadingId.value = null;
  }
}
</script>

<style scoped>
.opinion-layout {
  display: grid;
  gap: 28px;
  /* 链式 min-width:0：让内部栅格项可收缩到容器宽，宽表格才能被 max-width:100% 夹住并横向滚动，
     而不是把整列阅读栏撑宽。 */
  min-width: 0;
}

.opinion-title {
  font-size: clamp(28px, 4vw, 40px);
  line-height: 1.05;
}

.opinion-title-enter-active {
  transition: opacity 0.4s var(--ease), transform 0.4s var(--ease);
}

.opinion-title-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}

.message-list {
  display: grid;
  gap: 28px;
  min-width: 0;
}

.message-block {
  display: grid;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--rule);
  min-width: 0;
}

.message-enter-active {
  transition: opacity 0.45s var(--ease), transform 0.45s var(--ease);
}

.message-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.message-move {
  transition: transform 0.45s var(--ease);
}

@media (prefers-reduced-motion: reduce) {
  .message-enter-active,
  .message-move {
    transition: none;
  }

  .message-enter-from {
    opacity: 1;
    transform: none;
  }
}

.message-role-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: var(--control-h-sm);
}

.message-role {
  font-size: var(--font-xxs);
  letter-spacing: 0.08em;
  color: var(--muted);
}

.user-quote {
  margin: 0;
  padding-left: 14px;
  border-left: 3px solid var(--ink);
  font-family: var(--serif-body);
  font-size: 16px;
  font-style: italic;
  line-height: 1.65;
  color: var(--ink-2);
}

.assistant-body {
  margin: 0;
  padding-left: 14px;
  border-left: 3px solid var(--accent);
  min-width: 0;
}

.assistant-pending {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--muted);
  font-style: italic;
}

.answer-now-enter-active {
  transition: opacity 0.25s var(--ease), transform 0.25s var(--ease);
}

.answer-now-enter-from {
  opacity: 0;
  transform: translateY(2px);
}

@media (prefers-reduced-motion: reduce) {
  .answer-now-enter-active {
    transition: none;
  }

  .answer-now-enter-from {
    opacity: 1;
    transform: none;
  }
}

.typing-dots {
  display: inline-flex;
  align-items: flex-end;
  gap: 4px;
  padding-bottom: 3px;
}

.typing-dots i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent);
  opacity: 0.35;
  animation: typing-bounce 1.2s var(--ease) infinite;
}

.typing-dots i:nth-child(2) {
  animation-delay: 0.18s;
}

.typing-dots i:nth-child(3) {
  animation-delay: 0.36s;
}

@keyframes typing-bounce {
  0%,
  60%,
  100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-4px);
  }
}

.assistant-empty {
  color: var(--muted);
}

.markdown-body :deep(p) {
  margin: 0 0 10px;
  line-height: 1.7;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 0 0 10px;
  padding-left: 20px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 14px 0 8px;
  font-family: var(--serif-display);
  font-weight: 400;
}

.markdown-body :deep(table) {
  display: block;
  width: max-content;
  max-width: 100%;
  margin: 0 0 12px;
  overflow-x: auto;
  border-collapse: collapse;
  font-family: var(--sans);
  font-size: 13px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 7px 12px;
  border: 1px solid var(--rule);
  text-align: left;
  vertical-align: top;
  line-height: 1.55;
}

.markdown-body :deep(thead th) {
  background: var(--paper-2);
  font-weight: 600;
  white-space: nowrap;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.18s var(--ease);
}

.message-block:hover .answer-actions,
.answer-actions:focus-within {
  opacity: 1;
}

@media (hover: none) {
  .answer-actions {
    opacity: 1;
  }
}

.answer-action {
  padding: 3px 10px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
  color: var(--muted);
  font-size: var(--font-xxs);
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}

.answer-action:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent-deep);
}

.answer-action--done {
  border-color: var(--accent-deep);
  color: var(--accent-deep);
  background: var(--accent-soft);
}

.feedback-modal {
  display: grid;
  gap: 12px;
}

.feedback-tip {
  margin: 0;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.6;
}

.risk-banner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--danger, #d97706);
  border-radius: 4px;
  background: var(--danger-soft, #fff6ed);
  color: var(--danger-deep, #92400e);
  font-family: var(--sans);
  font-size: 12.5px;
  line-height: 1.55;
}

.risk-icon {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--danger, #d97706);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.answer-action:disabled {
  opacity: 0.55;
  cursor: progress;
}

.tldr {
  margin: 0;
  font-size: 16px;
}

.steps {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 20px;
}

.step {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 14px;
}

.step-no {
  font-size: 18px;
  color: var(--muted-2);
}

.step strong {
  display: block;
  margin-bottom: 6px;
  font-family: var(--serif-display);
  font-size: 18px;
  font-weight: 400;
}

.step p {
  margin: 0;
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.7;
  color: var(--ink-2);
}

.cite-ref,
.markdown-body :deep(.cite-ref) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  margin: 0 1px 0 3px;
  padding: 1px 6px;
  border: 0;
  border-radius: 100px;
  background: var(--paper-sunk);
  color: var(--ink-2);
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 500;
  line-height: 1.4;
  vertical-align: super;
  cursor: pointer;
  transition: color 0.15s var(--ease), background 0.15s var(--ease);
}

.cite-ref:hover,
.markdown-body :deep(.cite-ref:hover) {
  color: var(--accent);
  background: var(--accent-glow);
}

.cite-ref--active,
.markdown-body :deep(.cite-ref--active) {
  color: var(--accent);
  background: var(--accent-glow);
}
</style>
