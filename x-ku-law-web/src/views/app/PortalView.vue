<template>
  <section class="page portal">
    <header class="portal-hero">
      <div class="section-kicker">§ 检索门户 · Search Portal</div>
      <h1 class="portal-title">检索 · 问答 · 比对，<em>一站式理解法规变化。</em></h1>

      <div class="portal-modes" role="tablist">
        <button
          v-for="m in modes"
          :key="m.key"
          type="button"
          role="tab"
          class="portal-mode"
          :class="{ active: mode === m.key }"
          :aria-selected="mode === m.key"
          @click="mode = m.key"
        >
          <strong>{{ m.label }}</strong>
          <span class="mono portal-mode-tag">{{ m.tag }}</span>
        </button>
      </div>

      <form class="portal-search" @submit.prevent="submit">
        <Search :size="19" class="portal-search-icon" />
        <input
          v-model="keyword"
          class="portal-search-input"
          :placeholder="activeMode.placeholder"
          aria-label="检索法规"
        />
        <XButton variant="primary" type="submit" class="portal-search-btn">
          {{ mode === 'ai' ? '提问' : '检索' }}
        </XButton>
      </form>
    </header>

    <PageState v-if="error" :error="error" />

    <section class="highlights hairline">
      <div
        v-for="(s, i) in statItems"
        :key="s.label"
        class="highlight-cell"
        :class="{ 'has-rule': i < statItems.length - 1 }"
      >
        <span class="t-meta-cap">{{ s.kind }}</span>
        <div class="highlight-value">
          <Skeleton v-if="loading" variant="stat" width="72px" />
          <strong v-else class="num">{{ s.value }}</strong>
        </div>
        <span class="highlight-label">{{ s.label }}</span>
      </div>
    </section>

    <section class="portal-trending">
      <div class="portal-col-head">
        <div class="section-kicker">§ 热点检索 · Trending</div>
        <span class="mono portal-col-caption">近 7 日</span>
      </div>
      <SkeletonList v-if="loading" :count="6" />
      <EmptyState
        v-else-if="!trending.length"
        title="暂无热点检索"
        description="近期的检索热词会出现在这里。"
      />
      <ol v-else class="portal-rank">
        <li v-for="(t, i) in trending" :key="t.keyword" class="portal-rank-row">
          <button type="button" class="portal-rank-btn" @click="searchKeyword(t.keyword)">
            <span class="num portal-rank-no">{{ String(i + 1).padStart(2, '0') }}</span>
            <span class="portal-rank-kw">{{ t.keyword }}</span>
            <span v-if="t.heat > 0" class="mono portal-rank-heat">▲ {{ t.heat }}</span>
          </button>
        </li>
      </ol>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@lucide/vue';
import XButton from '@/components/common/XButton.vue';
import PageState from '@/components/common/PageState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import Skeleton from '@/components/common/Skeleton.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import { getHomeOverview } from '@/api/portal';
import { lawSearchTo } from '@/router/navigation';
import { resolveApiError } from '@/utils/apiError';
import type { HomeOverview, PortalTrending } from '@/types/portal';

const router = useRouter();
const keyword = ref('');
const mode = ref<'keyword' | 'ai'>('keyword');
const loading = ref(false);
const error = ref('');
const overview = ref<HomeOverview | null>(null);

const modes = [
  {
    key: 'keyword' as const,
    label: '快速检索',
    tag: 'KEYWORD',
    placeholder: '搜索关键词'
  },
  {
    key: 'ai' as const,
    label: 'AI 智询',
    tag: 'COPILOT',
    placeholder: '描述你的问题'
  }
];

const activeMode = computed(() => modes.find((m) => m.key === mode.value) ?? modes[0]);

const trending = computed<PortalTrending[]>(() => overview.value?.trending ?? []);

const statItems = computed(() => [
  { kind: '现行', label: '现行法规收录', value: formatNumber(overview.value?.corpusCount) },
  { kind: '今日', label: '今日法规更新', value: formatNumber(overview.value?.todayUpdateCount) },
  { kind: '层级', label: '效力层级覆盖', value: formatNumber(overview.value?.levelCount) },
  { kind: '地区', label: '地区规则覆盖', value: formatNumber(overview.value?.regionCount) }
]);

function formatNumber(n?: number) {
  return (n ?? 0).toLocaleString('en-US');
}

function submit() {
  const q = keyword.value.trim();
  if (mode.value === 'ai') {
    router.push({ name: 'ai.chat', query: q ? { q } : undefined });
    return;
  }
  router.push(lawSearchTo(q || undefined));
}

function searchKeyword(kw: string) {
  keyword.value = kw;
  router.push(lawSearchTo(kw));
}

async function loadOverview() {
  loading.value = true;
  error.value = '';
  try {
    overview.value = await getHomeOverview();
  } catch (e) {
    error.value = resolveApiError(e, '首页数据加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.portal {
  display: grid;
  gap: 40px;
  max-width: 1040px;
}

.portal-hero {
  display: grid;
  justify-items: center;
  gap: 14px;
  width: 100%;
  max-width: 760px;
  margin: 0 auto;
  padding-top: 26px;
  text-align: center;
}

.portal-title {
  margin-top: 4px;
  font-family: var(--serif-display);
  font-size: clamp(28px, 3.4vw, 40px);
  font-weight: 400;
  line-height: 1.14;
  color: var(--ink);
}

.portal-title em {
  font-style: italic;
  color: var(--accent);
}

/* 模式切换：方角分段控件 */
.portal-modes {
  display: inline-flex;
  margin-top: 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  overflow: hidden;
  background: var(--paper-2);
}

.portal-mode {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  padding: 8px 18px;
  border: none;
  background: none;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.15s var(--ease), background 0.15s var(--ease);
}

.portal-mode + .portal-mode {
  border-left: 1px solid var(--rule);
}

.portal-mode strong {
  font-family: var(--sans);
  font-size: 12px;
  font-weight: 600;
}

.portal-mode-tag {
  font-size: 10px;
  letter-spacing: 0.12em;
  color: var(--muted-2);
}

.portal-mode:hover {
  color: var(--ink-2);
}

.portal-mode.active {
  background: var(--ink);
  color: var(--paper);
}

/* 激活态在深底上，英文标签用半透明白保证可读 */
.portal-mode.active .portal-mode-tag {
  color: rgba(255, 255, 255, 0.6);
}

/* 搜索框 — 页面视觉焦点，方角 */
.portal-search {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  margin-top: 4px;
  padding: 7px 7px 7px 18px;
  border: 1px solid var(--ink-3);
  border-radius: 4px;
  background: var(--paper);
  transition: border-color 0.15s var(--ease), box-shadow 0.15s var(--ease);
}

.portal-search:focus-within {
  border-color: var(--ink);
  box-shadow: 0 0 0 3px var(--accent-soft);
}

.portal-search-icon {
  flex-shrink: 0;
  color: var(--muted);
}

.portal-search-input {
  flex: 1;
  min-width: 0;
  height: 48px;
  border: none;
  background: none;
  font-family: var(--serif-body);
  font-size: 17px;
  color: var(--ink);
  outline: none;
  text-align: left;
}

.portal-search-input::placeholder {
  color: var(--muted-2);
}

/* 覆盖 XButton 的 pill 圆角，统一为小方角 */
.portal-search :deep(.portal-search-btn) {
  height: 48px;
  padding-inline: 22px;
  border-radius: 4px;
}

/* Highlights（覆盖范围）— 编辑型统计条 */
.highlights {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding-top: 22px;
}

.highlight-cell {
  padding: 0 24px 0 0;
}

.highlight-cell.has-rule {
  border-right: 1px solid var(--rule);
}

.highlight-cell + .highlight-cell {
  padding-left: 24px;
}

.highlight-value {
  display: flex;
  align-items: baseline;
  min-height: 44px;
  margin-top: 8px;
}

.highlight-cell .num {
  font-size: 40px;
  line-height: 1;
  color: var(--ink);
}

.highlight-label {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-2);
}

/* 热点检索（整行） */
.portal-col-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
}

.portal-col-caption {
  color: var(--muted);
  font-size: 11px;
}

.portal-rank {
  list-style: none;
  border-top: 1px solid var(--ink);
}

.portal-rank-btn {
  display: flex;
  align-items: center;
  gap: 18px;
  width: 100%;
  padding: 15px 8px;
  border: none;
  border-bottom: 1px solid var(--rule);
  background: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.portal-rank-btn:hover {
  background: var(--paper-2);
}

.portal-rank-no {
  flex-shrink: 0;
  width: 30px;
  font-size: 22px;
  line-height: 1;
  color: var(--muted-2);
}

.portal-rank-kw {
  flex: 1;
  min-width: 0;
  font-family: var(--serif-body);
  font-size: 16px;
  color: var(--ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.portal-rank-heat {
  flex-shrink: 0;
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 720px) {
  .highlights {
    grid-template-columns: repeat(2, 1fr);
    gap: 22px 0;
  }

  .highlight-cell.has-rule:nth-child(2n) {
    border-right: none;
  }
}
</style>
