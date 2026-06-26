<template>
  <section class="topics">
    <div class="section-kicker">§ 专题</div>
    <div class="topics-grid">
      <button
        v-for="topic in topics"
        :key="topic.keyword"
        type="button"
        class="topic-tile"
        @click="emit('select', topic)"
      >
        <span class="mono topic-count">{{ topic.count }}</span>
        <strong>{{ topic.label }}</strong>
        <span class="topic-hint">{{ topic.hint }}</span>
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
export interface QuickTopic {
  label: string;
  hint: string;
  keyword: string;
  count: string | number;
}

defineProps<{
  topics: QuickTopic[];
}>();

const emit = defineEmits<{
  select: [topic: QuickTopic];
}>();
</script>

<style scoped>
.topics {
  display: grid;
  gap: 18px;
  padding-top: 8px;
  border-top: 1px solid var(--ink);
}

.topics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
}

.topic-tile {
  display: grid;
  gap: 8px;
  padding: 20px 24px 24px 0;
  border: 0;
  border-right: 1px solid var(--rule);
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.topic-tile + .topic-tile {
  padding-left: 24px;
}

.topic-tile:last-child {
  border-right: 0;
}

.topic-tile:hover {
  background: var(--paper-2);
}

.topic-count {
  font-size: var(--font-xs);
  color: var(--muted);
}

.topic-tile strong {
  font-family: var(--serif-display);
  font-size: 22px;
  font-weight: 400;
  line-height: 1.1;
  color: var(--ink);
}

.topic-hint {
  font-family: var(--serif-body);
  font-size: 13px;
  color: var(--ink-3);
}

@media (max-width: 900px) {
  .topics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .topic-tile:nth-child(2) {
    border-right: 0;
  }

  .topic-tile:nth-child(odd):not(:first-child) {
    padding-left: 0;
  }
}
</style>
