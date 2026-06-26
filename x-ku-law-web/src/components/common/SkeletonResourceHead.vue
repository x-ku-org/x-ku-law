<template>
  <div class="sk-resource" aria-hidden="true">
    <header class="sk-resource-head">
      <div class="sk-resource-copy">
        <span class="sk-kicker sk-shimmer" />
        <Skeleton width="42%" />
        <Skeleton width="68%" :lines="2" />
      </div>
      <span class="sk-create sk-shimmer" />
    </header>
    <section class="sk-toolbar">
      <span class="sk-input sk-shimmer" />
      <span class="sk-select sk-shimmer" />
      <span class="sk-btn sk-shimmer" />
    </section>
    <div class="sk-meta">
      <span class="sk-meta-item sk-shimmer" />
      <span class="sk-meta-title sk-shimmer" />
      <span class="sk-meta-count sk-shimmer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';
</script>

<style scoped>
.sk-resource {
  display: grid;
  gap: 20px;
}

.sk-resource-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.sk-resource-copy {
  display: grid;
  gap: 12px;
  flex: 1;
}

.sk-kicker {
  width: 96px;
  height: 10px;
  border-radius: 2px;
}

.sk-create {
  width: 88px;
  height: 36px;
  border-radius: 4px;
  flex-shrink: 0;
}

.sk-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px auto;
  gap: 12px;
  align-items: stretch;
}

.sk-input {
  height: 36px;
  border-radius: 4px;
}

.sk-select {
  height: 36px;
  border-radius: 4px;
}

.sk-btn {
  width: 72px;
  height: 36px;
  border-radius: 4px;
}

.sk-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid var(--rule);
  border-bottom: 1px solid var(--rule);
}

.sk-meta-item {
  width: 56px;
  height: 10px;
  border-radius: 2px;
}

.sk-meta-title {
  width: 96px;
  height: 14px;
  border-radius: 2px;
}

.sk-meta-count {
  width: 72px;
  height: 10px;
  border-radius: 2px;
}

@media (max-width: 720px) {
  .sk-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
