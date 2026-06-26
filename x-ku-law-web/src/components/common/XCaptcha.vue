<template>
  <button
    type="button"
    class="x-captcha"
    :title="'点击刷新验证码'"
    aria-label="点击刷新图形验证码"
    @click="refresh"
  >
    <canvas ref="canvasRef" :width="width" :height="height"></canvas>
  </button>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    /** 当前验证码（大写），通过 v-model 暴露给父组件比对 */
    modelValue?: string;
    width?: number;
    height?: number;
    length?: number;
  }>(),
  { modelValue: '', width: 120, height: 40, length: 4 }
);
const emit = defineEmits<{ 'update:modelValue': [code: string] }>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
// 去除易混淆字符 0/O/1/I/l
const CHARS = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789';

function randomColor(min: number, max: number) {
  const r = Math.floor(Math.random() * (max - min) + min);
  const g = Math.floor(Math.random() * (max - min) + min);
  const b = Math.floor(Math.random() * (max - min) + min);
  return `rgb(${r},${g},${b})`;
}

function draw() {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const { width, height, length } = props;
  let code = '';
  for (let i = 0; i < length; i += 1) {
    code += CHARS[Math.floor(Math.random() * CHARS.length)];
  }
  emit('update:modelValue', code);

  ctx.clearRect(0, 0, width, height);
  ctx.fillStyle = '#f3f4f7';
  ctx.fillRect(0, 0, width, height);

  // 字符
  const step = width / (length + 1);
  for (let i = 0; i < length; i += 1) {
    ctx.font = `${Math.floor(height * 0.6)}px var(--mono, monospace)`;
    ctx.fillStyle = randomColor(40, 110);
    ctx.textBaseline = 'middle';
    ctx.save();
    const x = step * (i + 1);
    const y = height / 2 + (Math.random() * 6 - 3);
    ctx.translate(x, y);
    ctx.rotate((Math.random() * 40 - 20) * (Math.PI / 180));
    ctx.fillText(code[i], -step * 0.3, 0);
    ctx.restore();
  }

  // 干扰线
  for (let i = 0; i < 3; i += 1) {
    ctx.strokeStyle = randomColor(150, 200);
    ctx.beginPath();
    ctx.moveTo(Math.random() * width, Math.random() * height);
    ctx.lineTo(Math.random() * width, Math.random() * height);
    ctx.stroke();
  }

  // 干扰点
  for (let i = 0; i < 24; i += 1) {
    ctx.fillStyle = randomColor(120, 200);
    ctx.beginPath();
    ctx.arc(Math.random() * width, Math.random() * height, 1, 0, Math.PI * 2);
    ctx.fill();
  }
}

function refresh() {
  draw();
}

onMounted(draw);
defineExpose({ refresh });
</script>

<style scoped>
.x-captcha {
  display: inline-flex;
  padding: 0;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: var(--paper);
  cursor: pointer;
  overflow: hidden;
  line-height: 0;
}

.x-captcha:hover {
  border-color: var(--muted-2);
}

.x-captcha canvas {
  display: block;
}
</style>
