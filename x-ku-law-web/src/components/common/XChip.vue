<template>
  <span class="x-chip" :class="`x-chip--${tone}`">
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ tone?: 'default' | 'accent' | 'gold' | 'rose' | 'moss' | 'outline' }>(), {
  tone: 'default'
});
</script>

<style scoped>
.x-chip {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  max-width: 100%;
  height: 22px;
  padding: 0 9px;
  border-radius: 4px;
  background: var(--paper-sunk);
  color: var(--ink-2);
  font-size: var(--font-xs);
  font-weight: 600;
  letter-spacing: 0;
  white-space: nowrap;
}

.x-chip--accent {
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.x-chip--gold {
  background: var(--gold-soft);
  color: var(--gold);
}

.x-chip--rose {
  background: var(--rose-soft);
  color: var(--rose);
}

.x-chip--moss {
  background: var(--moss-soft);
  color: var(--moss);
}

.x-chip--outline {
  border: 1px solid var(--rule-strong);
  background: transparent;
}
</style>
