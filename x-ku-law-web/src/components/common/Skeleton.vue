<template>
  <div class="sk" :class="[`sk--${variant}`, { 'sk--multiline': lines > 1 }]">
    <span
      v-for="n in lines"
      :key="n"
      class="sk-bar sk-shimmer"
      :style="barStyle(n)"
    />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    variant?: 'text' | 'stat' | 'block';
    width?: string;
    lines?: number;
  }>(),
  {
    variant: 'text',
    width: '100%',
    lines: 1
  }
);

function barStyle(lineIndex: number) {
  const w = lineIndex === props.lines && props.lines > 1 ? '72%' : props.width;
  return { width: w };
}
</script>

<style scoped>
.sk {
  display: grid;
  gap: 8px;
}

.sk--multiline {
  gap: 10px;
}

.sk-bar {
  display: block;
  height: 12px;
  border-radius: 3px;
}

.sk--stat .sk-bar {
  height: 48px;
  border-radius: 2px;
}

.sk--block .sk-bar {
  height: 100%;
  min-height: 80px;
  border-radius: 4px;
}
</style>
