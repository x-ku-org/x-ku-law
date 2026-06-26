<template>
  <div class="sk-messages">
    <div
      v-for="n in count"
      :key="n"
      class="sk-message"
      :class="n % 2 === 1 ? 'sk-message--user' : 'sk-message--assistant'"
    >
      <span class="sk-role sk-shimmer" />
      <div class="sk-body" :class="n % 2 === 1 ? 'sk-body--user' : 'sk-body--assistant'">
        <Skeleton :width="n % 2 === 1 ? '54%' : '94%'" :lines="n % 2 === 1 ? 1 : 3" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue';

withDefaults(defineProps<{ count?: number }>(), { count: 3 });
</script>

<style scoped>
.sk-messages {
  display: grid;
  gap: 28px;
}

.sk-message {
  display: grid;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--rule);
}

.sk-role {
  width: 56px;
  height: 9px;
  border-radius: 2px;
}

.sk-body {
  padding-left: 14px;
  border-left: 3px solid var(--paper-sunk);
}

.sk-body--user {
  border-left-color: var(--rule-strong);
}

.sk-body--assistant {
  border-left-color: var(--accent-glow);
}
</style>
