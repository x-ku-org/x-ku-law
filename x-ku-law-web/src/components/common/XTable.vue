<template>
  <div class="table-wrap">
    <table :class="{ 'table--dense': dense }">
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key" :style="columnStyle(column)">{{ column.label }}</th>
          <th v-if="$slots.actions" class="actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="row in rows"
          :key="String(row[rowKey])"
          :class="{ clickable: rowClickable }"
          @click="onRowClick(row)"
        >
          <td v-for="column in columns" :key="column.key" :style="columnStyle(column)">
            <slot :name="`cell-${column.key}`" :row="row" :value="row[column.key]">
              <span class="cell-text" :title="formatCell(row[column.key])">{{ formatCell(row[column.key]) }}</span>
            </slot>
          </td>
          <td v-if="$slots.actions" class="actions">
            <div class="actions-inner">
              <slot name="actions" :row="row" />
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <EmptyState v-if="!rows.length" :title="emptyTitle" :description="emptyDescription">
      <slot name="empty" />
    </EmptyState>
  </div>
</template>

<script setup lang="ts">
import EmptyState from './EmptyState.vue';
import { labelOf } from '@/utils/labels';

export interface XTableColumn {
  key: string;
  label: string;
  width?: string;
}

const props = withDefaults(
  defineProps<{
    columns: XTableColumn[];
    rows: Record<string, unknown>[];
    rowKey?: string;
    rowClickable?: boolean;
    emptyTitle?: string;
    emptyDescription?: string;
    dense?: boolean;
  }>(),
  {
    rowKey: 'id',
    rowClickable: false,
    emptyTitle: '暂无记录',
    emptyDescription: '当前筛选条件下没有可展示的数据。',
    dense: false
  }
);

const emit = defineEmits<{
  rowClick: [row: Record<string, unknown>];
}>();

function onRowClick(row: Record<string, unknown>) {
  if (!props.rowClickable) return;
  emit('rowClick', row);
}

function formatCell(value: unknown) {
  return labelOf(value);
}

function columnStyle(column: XTableColumn) {
  if (!column.width) return undefined;
  return { width: column.width, minWidth: column.width };
}
</script>

<style scoped>
.table-wrap {
  overflow: auto;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

th,
td {
  padding: 12px 14px;
  border-bottom: 1px solid var(--rule);
  text-align: left;
  vertical-align: top;
}

th {
  background: var(--paper-2);
  color: var(--muted);
  font-family: var(--sans);
  font-size: var(--font-xs);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

td {
  color: var(--ink-2);
  font-size: 13px;
}

.cell-text {
  display: -webkit-box;
  max-width: 44ch;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.45;
}

.table--dense th,
.table--dense td {
  padding: 9px 12px;
}

tr:hover td {
  background: var(--paper-2);
}

tr.clickable {
  cursor: pointer;
}

.actions {
  min-width: 140px;
  text-align: right;
  white-space: nowrap;
}

.actions-inner {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  justify-content: flex-end;
}
</style>
