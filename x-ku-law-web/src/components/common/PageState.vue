<template>
  <template v-if="loading">
    <slot v-if="mode === 'slot' && $slots.loading" name="loading" />
    <div v-else-if="mode === 'overlay'" class="state state--overlay">
      <div class="state-overlay-inner">
        <Diamond />
        <span>正在读取数据</span>
      </div>
    </div>
    <div v-else class="state">
      <Diamond />
      <span>正在读取数据</span>
    </div>
  </template>
  <div v-else-if="error" class="state state--error">
    <AlertTriangle :size="16" />
    <span>{{ error }}</span>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { AlertTriangle } from '@lucide/vue';
import Diamond from '@/components/brand/Diamond.vue';

withDefaults(
  defineProps<{
    loading?: boolean;
    error?: string;
    mode?: 'inline' | 'overlay' | 'slot';
  }>(),
  {
    loading: false,
    error: '',
    mode: 'inline'
  }
);
</script>

<style scoped>
.state {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  min-height: 42px;
  color: var(--muted);
  font-family: var(--mono);
  font-size: 12px;
}

.state--overlay {
  display: grid;
  place-items: center;
  min-height: 200px;
  width: 100%;
}

.state-overlay-inner {
  display: inline-flex;
  gap: 10px;
  align-items: center;
}

.state--error {
  color: var(--rose);
}
</style>
