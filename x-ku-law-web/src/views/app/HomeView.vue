<template>
  <section class="page home">
    <header class="masthead">
      <div>
        <div class="section-kicker">X · KU · § 工作台</div>
        <h1 class="display" v-html="mastheadHtml" />
        <p class="masthead-summary">{{ mastheadSummary }}</p>
      </div>
      <aside class="issue">
        <div class="mono issue-date">
          <span>{{ dateStr }}</span>
          <span>· 星期{{ weekday }}</span>
        </div>
        <CubeOrnament class="issue-cube" :size="100" />
        <XButton size="small" variant="ghost" :loading="loading" @click="loadWorkspace">刷新工作台</XButton>
      </aside>
    </header>

    <SkeletonHomeHighlights v-if="loading" />
    <section v-else class="highlights hairline-strong">
      <div v-for="(item, i) in highlightItems" :key="item.label" class="highlight-cell" :class="{ 'has-rule': i < 3 }">
        <span class="t-meta-cap">{{ item.kind }}</span>
        <div class="highlight-value">
          <strong class="num">{{ item.n }}</strong>
          <span v-if="item.delta" class="mono highlight-delta" :class="deltaClass(item.delta)">{{ item.delta }}</span>
        </div>
        <span class="highlight-label">{{ item.label }}</span>
        <small v-if="item.note">{{ item.note }}</small>
      </div>
    </section>

    <section class="hero">
      <div class="query">
        <div class="section-kicker">§ 探询 · Ask the corpus</div>
        <h2 class="hero-title"><em>查法规、找条款、</em>看版本变化。</h2>
        <form @submit.prevent="goSearch">
          <input v-model="keyword" placeholder="输入关键词，例如 数据出境、个人信息保护" />
          <XButton variant="primary" type="submit">
            <Search :size="15" />
            检索
          </XButton>
        </form>
        <SearchModeChips :modes="searchModes" @select="onSearchMode" />
      </div>

      <SkeletonHomeSpotlight v-if="loading" />
      <div v-else class="spotlight">
        <CubeOrnament class="cube" :size="260" />
        <div class="section-kicker spotlight-kicker">§ 重点 · 头条</div>
        <template v-if="spotlight">
          <XChip tone="accent" class="spotlight-chip">
            <Diamond :size="6" />
            订阅命中
          </XChip>
          <h2 class="spotlight-title">
            <em>{{ spotlight.title }}</em>
          </h2>
          <p class="spotlight-body">{{ spotlight.body }}</p>
          <div class="spotlight-actions">
            <XButton v-if="spotlight.documentId" variant="accent" size="small" @click="router.push(lawDetailTo(spotlight.documentId))">
              查看正文
            </XButton>
            <XButton
              v-if="spotlight.documentId"
              size="small"
              variant="ghost"
              class="spotlight-ghost"
              @click="router.push(lawCompareTo(spotlight.documentId))"
            >
              对比版本
            </XButton>
          </div>
        </template>
        <template v-else>
          <h2 class="spotlight-title"><em>从问题定位到条款依据。</em></h2>
          <p class="spotlight-body">输入业务问题，系统返回相关法规、具体条款、版本状态和引用来源。</p>
          <div class="spotlight-actions">
            <XButton variant="accent" size="small" @click="goSearch">开始检索</XButton>
            <XButton size="small" variant="ghost" class="spotlight-ghost" @click="router.push({ name: 'ai.chat' })">打开 AI 问答</XButton>
          </div>
        </template>
      </div>
    </section>

    <section class="workflow-strip" aria-label="工作流">
      <button type="button" class="workflow-step" @click="router.push(lawSearchTo(keyword || '个人信息保护'))">
        <span class="mono">01</span>
        <strong>检索法规</strong>
        <small>按关键词、文号、机关定位正文。</small>
      </button>
      <button type="button" class="workflow-step" :disabled="!recentLawId" @click="recentLawId && router.push(lawDetailTo(recentLawId))">
        <span class="mono">02</span>
        <strong>阅读条款</strong>
        <small>{{ recentLawId ? '回到最近打开的法规。' : '打开一部法规后可继续阅读。' }}</small>
      </button>
      <button type="button" class="workflow-step" :disabled="!recentLawId" @click="recentLawId && router.push(lawCompareTo(recentLawId))">
        <span class="mono">03</span>
        <strong>对比版本</strong>
        <small>{{ recentLawId ? '查看沿革和逐条差异。' : '需要先有最近阅读法规。' }}</small>
      </button>
      <button type="button" class="workflow-step" @click="router.push({ name: 'ai.chat' })">
        <span class="mono">04</span>
        <strong>证据问答</strong>
        <small>用历史会话追踪引用依据。</small>
      </button>
    </section>

    <section class="columns hairline-strong">
      <PageState v-if="error" :error="error" />
      <div class="col-alerts">
        <div class="column-head">
          <div class="section-kicker">§ Alerts</div>
          <RouterLink class="mono col-link" :to="{ name: 'app.alerts' }">查看全部 →</RouterLink>
        </div>
        <SkeletonList v-if="loading" :count="3" />
        <EditorialFeed
          v-else
          :items="alertFeedItems"
          empty-title="暂无订阅命中"
          empty-description="创建订阅规则后，新的法规变化会出现在这里。"
          @select="onAlertSelect"
        />
      </div>

      <div v-if="loading" class="col-pulse">
        <SkeletonPulseCard />
      </div>
      <div v-else class="col-pulse">
        <PulseCard
          :score="pulseScore"
          :percent="pulsePercent"
          :delta="pulseDelta"
          :subtitle="pulseSubtitle"
          :note="pulseNote"
          :link-to="{ name: 'app.messages' }"
        />
      </div>

      <div class="col-copilot">
        <div class="section-kicker">§ Copilot</div>
        <blockquote class="copilot-quote marginalia">
          「输入问题后，回答应包含法规名称、条款位置、版本状态和可跳转引用。」
        </blockquote>
        <div class="copilot-stats mono">
          <span>会话入口</span>
          <strong>{{ aiHint }}</strong>
        </div>
        <XButton variant="primary" @click="router.push({ name: 'ai.chat' })">开启对话</XButton>
        <div class="quick-links">
          <RouterLink :to="{ name: 'app.savedSearches' }">保存检索</RouterLink>
          <RouterLink :to="{ name: 'app.subscriptions' }">订阅规则</RouterLink>
          <RouterLink :to="{ name: 'app.favorites' }">收藏夹</RouterLink>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@lucide/vue';
import CubeOrnament from '@/components/brand/CubeOrnament.vue';
import Diamond from '@/components/brand/Diamond.vue';
import EditorialFeed from '@/components/editorial/EditorialFeed.vue';
import type { EditorialFeedItem } from '@/components/editorial/EditorialFeed.vue';
import PulseCard from '@/components/editorial/PulseCard.vue';
import SearchModeChips from '@/components/editorial/SearchModeChips.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonHomeHighlights from '@/components/common/SkeletonHomeHighlights.vue';
import SkeletonHomeSpotlight from '@/components/common/SkeletonHomeSpotlight.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import SkeletonPulseCard from '@/components/common/SkeletonPulseCard.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import { searchLaws } from '@/api/law';
import { getInbox, getSubscriptionMatches } from '@/api/workspace';
import { getLastLawDocumentId } from '@/utils/recentLaw';
import { lawCompareTo, lawDetailTo, lawSearchTo } from '@/router/navigation';
import type { NotificationInbox, SubscriptionMatch } from '@/types/workspace';
import { resolveApiError } from '@/utils/apiError';
import { matchToFeedItem } from '@/utils/editorialFeed';

const router = useRouter();
const keyword = ref('');
const loading = ref(false);
const error = ref('');
const corpusDocCount = ref(0);
const inbox = ref<NotificationInbox[]>([]);
const matches = ref<SubscriptionMatch[]>([]);
const recentLawId = computed(() => getLastLawDocumentId());

const today = new Date();
const dateStr = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
const weekday = ['日', '一', '二', '三', '四', '五', '六'][today.getDay()];

const unreadInbox = computed(() => inbox.value.filter((n) => n.readStatus !== 'read' && n.readStatus !== 'READ').length);
const unreadMatches = computed(() => matches.value.filter((m) => m.readStatus !== 'read' && m.readStatus !== 'READ').length);

const mastheadHtml = computed(() => {
  const alerts = matches.value.length;
  const unread = unreadInbox.value + unreadMatches.value;
  if (!alerts && !unread) {
    return '工作台暂无待处理事项。';
  }
  return `有 <span class="num" style="color:var(--accent)">${alerts}</span> 条订阅命中需要查看，<br/><span class="num" style="color:var(--rose)">${unread}</span> 条未读事项待处理。`;
});

const mastheadSummary = computed(() => {
  const corpus = corpusDocCount.value ? `${corpusDocCount.value} 部法规` : '法规库';
  if (matches.value.length || unreadInbox.value || unreadMatches.value) {
    return `可检索 ${corpus}；订阅命中、站内通知和最近阅读会在工作台汇总。`;
  }
  return `可检索 ${corpus}；配置订阅后，法规变化会在这里汇总。`;
});

const highlightItems = computed(() => [
  { kind: '公开检索', label: '可检索法规规模', n: corpusDocCount.value, delta: undefined },
  { kind: '订阅命中', label: '待阅法规变化', n: matches.value.length, delta: unreadMatches.value ? `+${unreadMatches.value}` : undefined },
  { kind: '收件箱', label: '站内通知', n: inbox.value.length, delta: unreadInbox.value ? `+${unreadInbox.value}` : undefined },
  {
    kind: '工作台',
    label: '未读事项合计',
    n: unreadInbox.value + unreadMatches.value,
    delta: unreadInbox.value + unreadMatches.value ? '待处理' : undefined,
    note: undefined
  }
]);

const searchModes = [
  { key: 'search', label: '关键词检索', hint: '文号、条款、发布机关' },
  { key: 'ai', label: 'AI 可溯源问答', hint: '回答附条款引用' },
  { key: 'compare', label: '版本对比', hint: '逐条差异与沿革' }
];

const spotlight = computed(() => {
  const top = matches.value[0];
  if (!top) return null;
  return {
    title: top.documentTitle || `订阅命中 #${top.id}`,
    body: top.matchReason || '点击查看法规正文与版本沿革。',
    documentId: top.documentId
  };
});

const alertFeedItems = computed(() => matches.value.slice(0, 5).map(matchToFeedItem));

const pulseScore = computed(() => Math.max(0, 100 - (unreadInbox.value + unreadMatches.value) * 4));
const pulsePercent = computed(() => pulseScore.value);
const pulseDelta = computed(() => {
  const u = unreadInbox.value + unreadMatches.value;
  return u ? `▼ ${u} 未读` : '▲ 稳定';
});
const pulseSubtitle = computed(() => `${inbox.value.length} 条通知 · ${matches.value.length} 条命中`);
const pulseNote = computed(() => '基于收件箱与订阅命中的未读占比估算活跃度。');

const aiHint = computed(() => (getLastLawDocumentId() ? '可结合最近阅读提问' : '先检索一部法规'));

function deltaClass(delta: string) {
  if (delta.startsWith('+') || delta === '待处理') return 'up';
  if (delta.startsWith('▼')) return 'down';
  return '';
}

function goSearch() {
  router.push(lawSearchTo(keyword.value));
}

function onSearchMode(key: string) {
  if (key === 'ai') {
    router.push({ name: 'ai.chat' });
    return;
  }
  if (key === 'compare') {
    const id = getLastLawDocumentId();
    if (id) router.push(lawCompareTo(id));
    else router.push(lawSearchTo(keyword.value));
    return;
  }
  router.push(lawSearchTo(keyword.value));
}

function onAlertSelect(_item: EditorialFeedItem, index: number) {
  const match = matches.value[index];
  if (match?.documentId) {
    router.push(lawDetailTo(match.documentId));
    return;
  }
  router.push({ name: 'app.alerts' });
}

async function loadWorkspace() {
  loading.value = true;
  error.value = '';
  const [search, inboxResult, matchResult] = await Promise.allSettled([
    searchLaws({ pageNo: 1, pageSize: 1 }),
    getInbox(),
    getSubscriptionMatches()
  ]);
  if (search.status === 'fulfilled') {
    corpusDocCount.value = search.value.documentCount ?? 0;
  }
  if (inboxResult.status === 'fulfilled') inbox.value = inboxResult.value.list;
  if (matchResult.status === 'fulfilled') matches.value = matchResult.value.list;
  const rejected = [search, inboxResult, matchResult].find((item) => item.status === 'rejected');
  if (rejected?.status === 'rejected') {
    error.value = resolveApiError(rejected.reason, '部分数据暂时不可用，请稍后刷新。');
  }
  loading.value = false;
}

onMounted(loadWorkspace);
</script>

<style scoped>
.home {
  display: grid;
  gap: 34px;
}

.masthead {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
}

.masthead-summary {
  max-width: 62ch;
  margin: 18px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.65;
}

.issue {
  position: relative;
  display: grid;
  gap: 8px;
  justify-items: end;
  min-width: 140px;
  text-align: right;
}

.issue-date {
  display: flex;
  gap: 6px;
  font-size: 12px;
  color: var(--ink-2);
}

.issue-cube {
  margin-top: 8px;
  margin-right: -8px;
  opacity: 0.85;
}

.highlights {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding-bottom: 4px;
}

.highlight-cell {
  padding: 20px 24px 22px 0;
}

.highlight-cell + .highlight-cell {
  padding-left: 24px;
}

.highlight-cell.has-rule {
  border-right: 1px solid var(--rule);
}

.highlight-sk {
  display: grid;
  gap: 10px;
}

.highlight-value {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 10px;
}

.highlight-cell strong {
  font-size: 44px;
  line-height: 1;
  color: var(--ink);
}

.highlight-delta.up {
  color: var(--moss);
}

.highlight-delta.down {
  color: var(--rose);
}

.highlight-label {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-2);
}

.highlight-cell small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(300px, 0.8fr);
  gap: var(--gutter);
}

.query {
  display: grid;
  gap: 20px;
  align-content: center;
  padding: 24px 0;
}

.hero-title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 38px;
  font-weight: 400;
  line-height: 1.08;
}

form {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: stretch;
}

input {
  box-sizing: border-box;
  min-width: 0;
  height: var(--control-h-lg);
  padding: 0 14px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: var(--paper);
  color: var(--ink);
  line-height: 1;
  outline: 0;
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

input:hover {
  border-color: var(--muted-2);
}

input:focus {
  border-color: var(--ink);
  background: var(--paper-card);
}

form :deep(.x-button) {
  height: var(--control-h-lg);
  padding-inline: 16px;
}

.spotlight {
  position: relative;
  overflow: hidden;
  min-height: 380px;
  padding: 28px;
  border-radius: 4px;
  background: var(--ink);
  color: var(--paper);
}

.spotlight-kicker {
  color: rgba(255, 255, 255, 0.55);
}

.spotlight-chip {
  margin-bottom: 16px;
}

.spotlight-title {
  margin: 0 0 16px;
  font-family: var(--serif-display);
  font-size: 34px;
  font-weight: 400;
  line-height: 1.05;
  color: var(--paper-2);
}

.spotlight-body {
  max-width: 48ch;
  margin: 0 0 20px;
  font-family: var(--serif-body);
  font-size: 14px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.72);
}

.spotlight-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spotlight-ghost {
  color: var(--paper-2) !important;
  border-color: rgba(255, 255, 255, 0.25) !important;
}

.cube {
  position: absolute;
  right: -60px;
  top: -50px;
  opacity: 0.18;
  pointer-events: none;
}

.columns {
  display: grid;
  grid-template-columns: 5fr 4fr 3fr;
  gap: var(--gutter);
  padding-top: 30px;
}

.workflow-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border-top: 1px solid var(--ink);
  border-bottom: 1px solid var(--rule);
}

.workflow-step {
  display: grid;
  gap: 8px;
  min-height: 118px;
  padding: 20px 24px 20px 0;
  border: 0;
  border-right: 1px solid var(--rule);
  background: transparent;
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}

.workflow-step + .workflow-step {
  padding-left: 24px;
}

.workflow-step:last-child {
  border-right: 0;
}

.workflow-step:hover:not(:disabled) {
  background: var(--paper-2);
}

.workflow-step:disabled {
  color: var(--muted-2);
  cursor: not-allowed;
}

.workflow-step strong {
  font-family: var(--serif-display);
  font-size: 22px;
  font-weight: 400;
  line-height: 1.1;
}

.workflow-step small {
  max-width: 24ch;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 13px;
  line-height: 1.45;
}

.col-alerts,
.col-pulse,
.col-copilot {
  display: grid;
  gap: 14px;
  align-content: start;
}

.column-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.col-link {
  font-size: var(--font-xs);
  color: var(--muted);
  text-decoration: none;
}

.col-link:hover {
  color: var(--accent);
}

.copilot-quote {
  margin: 8px 0 0;
}

.copilot-stats {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
  font-size: var(--font-xs);
  color: var(--muted);
}

.copilot-stats strong {
  color: var(--ink);
}

.quick-links {
  display: grid;
  margin-top: 8px;
}

.quick-links a {
  padding: 12px 0;
  border-bottom: 1px solid var(--rule);
  font-family: var(--serif-body);
  color: var(--ink-2);
}

@media (max-width: 980px) {
  .highlights,
  .hero,
  .columns {
    grid-template-columns: 1fr;
  }

  .workflow-strip {
    grid-template-columns: 1fr;
  }

  .workflow-step {
    border-right: 0;
    border-bottom: 1px solid var(--rule);
    padding-right: 0;
  }

  .highlight-cell + .highlight-cell,
  .workflow-step + .workflow-step {
    padding-left: 0;
  }

  .highlight-cell.has-rule {
    border-right: 0;
    border-bottom: 1px solid var(--rule);
  }

  form {
    grid-template-columns: 1fr;
  }
}
</style>
