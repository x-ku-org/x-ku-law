<template>
  <header class="sk-search-head" aria-hidden="true">
    <span class="sk-kicker sk-shimmer" />
    <div class="sk-query-row">
      <span class="sk-query-line sk-shimmer" />
      <span class="sk-query-hint sk-shimmer" />
    </div>
    <span class="sk-stats sk-shimmer" />
  </header>
</template>

<style scoped>
.sk-search-head {
  display: grid;
  gap: 14px;
  margin-bottom: 26px;
}

.sk-kicker {
  width: 120px;
  height: 10px;
  border-radius: 2px;
}

.sk-query-row {
  position: relative;
}

.sk-query-line {
  display: block;
  width: 100%;
  height: clamp(36px, 5vw, 56px);
  border-radius: 0;
  border-bottom: 2px solid var(--rule);
  background-clip: padding-box;
}

.sk-query-hint {
  position: absolute;
  top: 8px;
  right: 0;
  width: 56px;
  height: 10px;
  border-radius: 2px;
}

.sk-stats {
  width: 220px;
  height: 12px;
  border-radius: 2px;
}
</style>
