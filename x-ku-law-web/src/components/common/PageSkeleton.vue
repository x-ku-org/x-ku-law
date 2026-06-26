<template>
  <section class="page page-sk" :class="[`page-sk--${layout}`, { 'page-sk--spinner': variant === 'spinner' }]">
    <div v-if="showStatus && variant !== 'spinner'" class="page-sk-status" role="status" aria-live="polite">
      <LoadingRing :label="statusText" />
      <span class="page-sk-status-text">{{ statusText }}</span>
    </div>

    <template v-if="variant === 'spinner'">
      <div class="page-sk-centered" role="status" aria-live="polite">
        <LoadingRing size="lg" :label="statusText" />
        <span class="page-sk-status-text page-sk-centered-label">{{ statusText }}</span>
      </div>
    </template>

    <template v-else-if="layout === 'generic'">
      <Skeleton width="28%" />
      <Skeleton width="55%" />
      <Skeleton width="100%" :lines="3" />
      <Skeleton variant="block" width="100%" />
    </template>

    <template v-else-if="layout === 'home'">
      <SkeletonHomeHighlights />
      <div class="page-sk-hero">
        <Skeleton width="55%" />
        <SkeletonHomeSpotlight />
      </div>
      <div class="page-sk-columns">
        <SkeletonList :count="3" />
        <SkeletonPulseCard />
      </div>
    </template>

    <template v-else-if="layout === 'search'">
      <SkeletonSearchQueryHead />
      <SkeletonSearchToolbar />
      <SkeletonResultMatrix />
      <SkeletonList variant="search" :count="5" />
    </template>

    <template v-else-if="layout === 'law'">
      <SkeletonLawMetaHead />
      <SkeletonVersionTimeline />
      <SkeletonLawReadingGrid />
    </template>

    <template v-else-if="layout === 'table'">
      <SkeletonResourceHead />
      <SkeletonTable />
    </template>

    <template v-else-if="layout === 'feed'">
      <SkeletonResourceHead />
      <SkeletonList :count="6" />
    </template>

    <template v-else-if="layout === 'compare'">
      <SkeletonCompareHeader />
      <SkeletonVersionTimeline />
      <SkeletonCompareSpread />
      <SkeletonCompareDiff />
    </template>

    <template v-else-if="layout === 'ai'">
      <SkeletonAiPage />
    </template>
  </section>
</template>

<script setup lang="ts">
import LoadingRing from '@/components/common/LoadingRing.vue';
import Skeleton from '@/components/common/Skeleton.vue';
import SkeletonAiPage from '@/components/common/SkeletonAiPage.vue';
import SkeletonCompareDiff from '@/components/common/SkeletonCompareDiff.vue';
import SkeletonCompareHeader from '@/components/common/SkeletonCompareHeader.vue';
import SkeletonCompareSpread from '@/components/common/SkeletonCompareSpread.vue';
import SkeletonHomeHighlights from '@/components/common/SkeletonHomeHighlights.vue';
import SkeletonHomeSpotlight from '@/components/common/SkeletonHomeSpotlight.vue';
import SkeletonLawMetaHead from '@/components/common/SkeletonLawMetaHead.vue';
import SkeletonLawReadingGrid from '@/components/common/SkeletonLawReadingGrid.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import SkeletonPulseCard from '@/components/common/SkeletonPulseCard.vue';
import SkeletonResourceHead from '@/components/common/SkeletonResourceHead.vue';
import SkeletonResultMatrix from '@/components/common/SkeletonResultMatrix.vue';
import SkeletonSearchQueryHead from '@/components/common/SkeletonSearchQueryHead.vue';
import SkeletonSearchToolbar from '@/components/common/SkeletonSearchToolbar.vue';
import SkeletonTable from '@/components/common/SkeletonTable.vue';
import SkeletonVersionTimeline from '@/components/common/SkeletonVersionTimeline.vue';

withDefaults(
  defineProps<{
    layout?: 'generic' | 'home' | 'search' | 'law' | 'table' | 'feed' | 'compare' | 'ai';
    variant?: 'skeleton' | 'spinner';
    showStatus?: boolean;
    statusText?: string;
  }>(),
  {
    layout: 'generic',
    variant: 'skeleton',
    showStatus: true,
    statusText: '正在载入'
  }
);
</script>

<style scoped>
.page-sk {
  display: grid;
  gap: 28px;
}

.page-sk--spinner {
  min-height: min(52vh, 420px);
  align-content: start;
}

.page-sk-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-sk-status-text {
  color: var(--muted);
  font-family: var(--mono);
  font-size: 13px;
  letter-spacing: 0.08em;
}

.page-sk-centered {
  display: grid;
  justify-items: center;
  gap: 14px;
  min-height: min(48vh, 360px);
  padding: 48px 0 24px;
  align-content: center;
}

.page-sk-centered-label {
  font-size: 13px;
  letter-spacing: 0.1em;
}

.page-sk-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(300px, 0.8fr);
  gap: var(--gutter);
  align-items: stretch;
}

.page-sk-columns {
  display: grid;
  grid-template-columns: 5fr 4fr 3fr;
  gap: var(--gutter);
  padding-top: 8px;
}

@media (max-width: 980px) {
  .page-sk-hero,
  .page-sk-columns {
    grid-template-columns: 1fr;
  }
}
</style>
