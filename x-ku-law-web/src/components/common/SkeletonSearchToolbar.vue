<template>
  <div class="sk-toolbar" aria-hidden="true">
    <span class="sk-filter-label sk-shimmer" />
    <div class="sk-chips">
      <span v-for="n in 6" :key="n" class="sk-chip sk-shimmer" />
    </div>
    <div class="sk-toolbar-extra">
      <span v-for="n in 4" :key="`sel-${n}`" class="sk-select sk-shimmer" />
      <span class="sk-button sk-shimmer" />
    </div>
  </div>
</template>

<style scoped>
.sk-toolbar {
  display: grid;
  gap: 12px;
  margin-bottom: 8px;
}

.sk-filter-label {
  width: 56px;
  height: 10px;
  margin-bottom: 4px;
  border-radius: 2px;
}

.sk-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sk-chip {
  width: 72px;
  height: 24px;
  border-radius: 4px;
}

.sk-chip:first-child {
  width: 48px;
}

.sk-toolbar-extra {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr)) auto;
  gap: 12px;
  align-items: stretch;
}

.sk-select {
  height: 36px;
  border-radius: 4px;
}

.sk-button {
  width: 88px;
  height: 36px;
  border-radius: 4px;
}

@media (max-width: 960px) {
  .sk-toolbar-extra {
    grid-template-columns: repeat(2, 1fr);
  }

  .sk-button {
    grid-column: span 2;
    width: 100%;
  }
}

@media (max-width: 720px) {
  .sk-toolbar-extra {
    grid-template-columns: 1fr;
  }

  .sk-button {
    grid-column: auto;
  }
}
</style>
