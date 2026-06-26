<template>
  <span class="diamond" :style="{ width: `${size}px`, height: `${size}px` }" aria-hidden="true" />
</template>

<script setup lang="ts">
withDefaults(defineProps<{ size?: number }>(), {
  size: 8
});
</script>

<style scoped>
.diamond {
  display: inline-block;
  flex: 0 0 auto;
  background: var(--accent);
  transform: rotate(45deg);
}
</style>
