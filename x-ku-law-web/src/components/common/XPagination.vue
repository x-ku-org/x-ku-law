<template>
  <footer class="pagination">
    <span class="mono total">共 {{ total }} 条</span>

    <label v-if="pageSizeOptions && pageSizeOptions.length" class="mono size-field">
      每页
      <select class="size-select" :value="pageSize" @change="onSizeChange">
        <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
      </select>
      条
    </label>

    <nav class="pages" aria-label="分页">
      <button type="button" class="page-btn" :disabled="pageNo <= 1" @click="goTo(pageNo - 1)">‹</button>
      <template v-for="(p, i) in pages" :key="`${p}-${i}`">
        <span v-if="p === '...'" class="page-gap">…</span>
        <button
          v-else
          type="button"
          class="page-btn"
          :class="{ active: p === pageNo }"
          @click="goTo(p)"
        >
          {{ p }}
        </button>
      </template>
      <button type="button" class="page-btn" :disabled="pageNo >= pageCount" @click="goTo(pageNo + 1)">›</button>
    </nav>

    <label class="mono jump-field">
      跳至
      <input
        v-model="jumpValue"
        class="jump-input"
        type="number"
        min="1"
        :max="pageCount"
        @keyup.enter="onJump"
      />
      页
    </label>
  </footer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    total: number;
    pageNo: number;
    pageSize: number;
    /** 提供时渲染每页条数选择器；变更通过 sizeChange 抛出 */
    pageSizeOptions?: number[];
    /** 页码按钮最大可见数量（含首尾），超出用省略号折叠 */
    maxButtons?: number;
  }>(),
  {
    pageSizeOptions: () => [],
    maxButtons: 7
  }
);

const emit = defineEmits<{
  change: [pageNo: number];
  sizeChange: [pageSize: number];
}>();

const jumpValue = ref('');

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / Math.max(props.pageSize, 1))));

const pages = computed<(number | '...')[]>(() => {
  const count = pageCount.value;
  const max = Math.max(props.maxButtons, 5);
  if (count <= max) {
    return Array.from({ length: count }, (_, i) => i + 1);
  }
  const inner = max - 2; // 预留首页与末页
  let start = Math.max(2, props.pageNo - Math.floor((inner - 1) / 2));
  let end = start + inner - 1;
  if (end >= count) {
    end = count - 1;
    start = end - inner + 1;
  }
  const result: (number | '...')[] = [1];
  if (start > 2) result.push('...');
  for (let p = start; p <= end; p += 1) result.push(p);
  if (end < count - 1) result.push('...');
  result.push(count);
  return result;
});

function goTo(target: number) {
  if (target < 1 || target > pageCount.value || target === props.pageNo) return;
  emit('change', target);
}

function onJump() {
  const n = Number(jumpValue.value);
  if (Number.isInteger(n)) goTo(n);
  jumpValue.value = '';
}

function onSizeChange(event: Event) {
  const next = Number((event.target as HTMLSelectElement).value);
  if (Number.isFinite(next) && next !== props.pageSize) emit('sizeChange', next);
}
</script>

<style scoped>
.pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
  justify-content: flex-end;
  padding-top: 18px;
  border-top: 1px solid var(--rule);
}

.total {
  color: var(--muted);
  font-size: 12px;
}

.size-field,
.jump-field {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--muted);
  font-size: 12px;
}

.size-select {
  height: 28px;
  padding: 0 6px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink);
  outline: 0;
}

.pages {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: transparent;
  color: var(--ink-2);
  font-family: var(--mono);
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease), color 0.15s var(--ease);
}

.page-btn:hover:not(:disabled) {
  border-color: var(--ink);
  color: var(--ink);
}

.page-btn.active {
  border-color: var(--accent);
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-gap {
  padding: 0 2px;
  color: var(--muted);
}

.jump-input {
  width: 52px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink);
  font-family: var(--mono);
  font-size: 12px;
  text-align: center;
  outline: 0;
}

.jump-input:focus,
.size-select:focus {
  border-color: var(--ink);
}
</style>
