<template>
  <section class="sk-spread" aria-hidden="true">
    <div class="sk-pane">
      <span class="sk-kicker sk-shimmer" />
      <span class="sk-select sk-shimmer" />
      <span class="sk-note sk-shimmer" />
    </div>
    <div class="sk-gutter">
      <span class="sk-diamond sk-shimmer" />
      <span class="sk-chip sk-shimmer" />
    </div>
    <div class="sk-pane">
      <span class="sk-kicker sk-shimmer" />
      <span class="sk-select sk-shimmer" />
      <span class="sk-note sk-shimmer" />
    </div>
  </section>
</template>

<style scoped>
.sk-spread {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 140px minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
}

.sk-pane {
  display: grid;
  gap: 16px;
  padding: 24px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

.sk-kicker {
  width: 72px;
  height: 10px;
  border-radius: 2px;
}

.sk-select {
  width: 100%;
  height: 36px;
  border-radius: 4px;
}

.sk-note {
  width: 68%;
  height: 12px;
  border-radius: 2px;
}

.sk-gutter {
  display: grid;
  gap: 14px;
  place-items: center;
  align-content: center;
}

.sk-diamond {
  width: 9px;
  height: 9px;
  border-radius: 1px;
  transform: rotate(45deg);
}

.sk-chip {
  width: 64px;
  height: 24px;
  border-radius: 4px;
}

@media (max-width: 860px) {
  .sk-spread {
    grid-template-columns: 1fr;
  }
}
</style>
