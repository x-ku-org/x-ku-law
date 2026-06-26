<template>
  <header class="admin-head">
    <div class="admin-head__main">
      <div class="section-kicker">{{ kicker }}</div>
      <h1 class="h1">{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="admin-head__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{
  kicker: string;
  title: string;
  description?: string;
}>();
</script>

<style scoped>
.admin-head {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--ink);
}

.admin-head__main {
  min-width: 0;
}

.admin-head p {
  max-width: 72ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
  line-height: 1.65;
}

.admin-head__actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

@media (max-width: 760px) {
  .admin-head {
    flex-direction: column;
  }
}
</style>
