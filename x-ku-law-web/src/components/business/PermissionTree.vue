<template>
  <div class="perm-tree" :class="`perm-tree--${mode}`">
    <div v-if="!visibleRows.length" class="perm-tree__empty">
      <EmptyState
        :title="keyword || typeFilter ? '没有匹配的资源' : '暂无权限资源'"
        :description="keyword || typeFilter ? '换个关键词或类型再试。' : '先新建一个顶级资源。'"
      />
    </div>

    <div
      v-for="row in visibleRows"
      :key="row.node.id"
      class="perm-row"
      :class="{ 'perm-row--selectable': mode === 'select' }"
      :style="{ paddingLeft: `${row.depth * 22 + 10}px` }"
    >
      <button
        v-if="row.hasChildren"
        type="button"
        class="perm-row__caret"
        :class="{ 'perm-row__caret--open': isOpen(row) }"
        :aria-label="isOpen(row) ? '收起' : '展开'"
        @click="toggleOpen(row.node.id)"
      >
        <ChevronRight :size="14" />
      </button>
      <span v-else class="perm-row__caret perm-row__caret--leaf" aria-hidden="true" />

      <!-- 选择模式：三态复选框 -->
      <button
        v-if="mode === 'select'"
        type="button"
        class="perm-check"
        role="checkbox"
        :aria-checked="checkState(row) === 'all' ? 'true' : checkState(row) === 'some' ? 'mixed' : 'false'"
        @click="toggleSelect(row)"
      >
        <span class="perm-check__mark" :class="`perm-check__mark--${checkState(row)}`" aria-hidden="true" />
      </button>

      <span class="perm-row__name" :title="row.node.permissionName">{{ row.node.permissionName || '未命名资源' }}</span>

      <XChip :tone="typeTone(row.node.permissionType)" class="perm-row__type">{{ typeLabel(row.node.permissionType) }}</XChip>

      <code v-if="row.node.permissionCode" class="perm-row__code">{{ row.node.permissionCode }}</code>
      <code v-if="mode === 'browse' && row.node.path" class="perm-row__path">{{ row.node.path }}</code>
      <span v-if="mode === 'browse' && row.node.requestMethod" class="perm-row__method mono">{{ row.node.requestMethod }}</span>

      <span class="perm-row__spacer" />

      <StatusBadge v-if="mode === 'browse'" :value="row.node.status || ''" />

      <span v-if="mode === 'browse'" class="perm-row__actions">
        <XButton size="small" variant="ghost" @click="emit('add-child', row.node)">+ 子资源</XButton>
        <XButton size="small" variant="ghost" @click="emit('edit', row.node)">编辑</XButton>
        <XButton size="small" variant="ghost" @click="emit('remove', row.node)">删除</XButton>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ChevronRight } from '@lucide/vue';
import EmptyState from '@/components/common/EmptyState.vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import type { PermissionRecord } from '@/types/admin';

const props = withDefaults(
  defineProps<{
    nodes: PermissionRecord[];
    mode?: 'browse' | 'select';
    /** select 模式：已选权限 id 集合（v-model） */
    modelValue?: number[];
    keyword?: string;
    typeFilter?: string;
  }>(),
  { mode: 'browse', modelValue: () => [], keyword: '', typeFilter: '' }
);

const emit = defineEmits<{
  'update:modelValue': [ids: number[]];
  edit: [node: PermissionRecord];
  remove: [node: PermissionRecord];
  'add-child': [node: PermissionRecord];
}>();

interface FlatRow {
  node: PermissionRecord;
  depth: number;
  hasChildren: boolean;
  ancestorIds: number[];
  subtreeIds: number[];
}

const expanded = ref<Set<number>>(new Set());
let initialized = false;

const selectedSet = computed(() => new Set(props.modelValue));

/** 扁平数组按 parentId 建树，再 DFS 前序展开为带 depth/subtree 的行表。 */
const allRows = computed<FlatRow[]>(() => {
  const byParent = new Map<number, PermissionRecord[]>();
  const ids = new Set(props.nodes.map((n) => n.id));
  for (const node of props.nodes) {
    const pid = node.parentId && ids.has(node.parentId) ? node.parentId : 0;
    if (!byParent.has(pid)) byParent.set(pid, []);
    byParent.get(pid)!.push(node);
  }
  for (const list of byParent.values()) {
    list.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0) || a.id - b.id);
  }

  const rows: FlatRow[] = [];
  const walk = (parentId: number, depth: number, ancestorIds: number[]): number[] => {
    const children = byParent.get(parentId) || [];
    const collected: number[] = [];
    for (const node of children) {
      const row: FlatRow = {
        node,
        depth,
        hasChildren: (byParent.get(node.id) || []).length > 0,
        ancestorIds,
        subtreeIds: []
      };
      rows.push(row);
      const sub = walk(node.id, depth + 1, [...ancestorIds, node.id]);
      row.subtreeIds = [node.id, ...sub];
      collected.push(node.id, ...sub);
    }
    return collected;
  };
  walk(0, 0, []);
  return rows;
});

// 默认展开全部，仅首次拿到数据时初始化一次。
watch(
  allRows,
  (rows) => {
    if (initialized || !rows.length) return;
    expanded.value = new Set(rows.filter((r) => r.hasChildren).map((r) => r.node.id));
    initialized = true;
  },
  { immediate: true }
);

const isFiltering = computed(() => Boolean(props.keyword.trim() || props.typeFilter));

/** 节点自身是否命中关键词 + 类型过滤。 */
function passes(node: PermissionRecord): boolean {
  const kw = props.keyword.trim().toLowerCase();
  const kwOk =
    !kw ||
    [node.permissionName, node.permissionCode, node.path].some((v) => (v || '').toLowerCase().includes(kw));
  const typeOk = !props.typeFilter || node.permissionType === props.typeFilter;
  return kwOk && typeOk;
}

/** 命中集合：自身命中或任一后代命中（保留祖先链）。 */
const keepSet = computed<Set<number>>(() => {
  const keep = new Set<number>();
  if (!isFiltering.value) return keep;
  const rows = allRows.value;
  const passSet = new Set(rows.filter((r) => passes(r.node)).map((r) => r.node.id));
  for (const row of rows) {
    if (passSet.has(row.node.id)) {
      keep.add(row.node.id);
      row.ancestorIds.forEach((id) => keep.add(id));
    }
  }
  return keep;
});

const visibleRows = computed<FlatRow[]>(() => {
  const rows = allRows.value;
  return rows.filter((row) => {
    if (isFiltering.value && !keepSet.value.has(row.node.id)) return false;
    // 过滤时强制全展开，便于看到命中分支。
    if (isFiltering.value) return true;
    return row.ancestorIds.every((id) => expanded.value.has(id));
  });
});

function isOpen(row: FlatRow) {
  return isFiltering.value || expanded.value.has(row.node.id);
}

function toggleOpen(id: number) {
  const next = new Set(expanded.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  expanded.value = next;
}

function checkState(row: FlatRow): 'all' | 'some' | 'none' {
  const sel = selectedSet.value;
  const total = row.subtreeIds.length;
  let count = 0;
  for (const id of row.subtreeIds) if (sel.has(id)) count++;
  if (count === 0) return 'none';
  if (count === total) return 'all';
  return 'some';
}

function toggleSelect(row: FlatRow) {
  const next = new Set(selectedSet.value);
  const allSelected = checkState(row) === 'all';
  for (const id of row.subtreeIds) {
    if (allSelected) next.delete(id);
    else next.add(id);
  }
  emit('update:modelValue', [...next]);
}

const TYPE_LABELS: Record<string, string> = { menu: '菜单', button: '按钮', api: '接口', data: '数据' };
function typeLabel(type?: string) {
  return type ? TYPE_LABELS[type] || type : '—';
}
function typeTone(type?: string): 'accent' | 'gold' | 'outline' | 'moss' | 'default' {
  switch (type) {
    case 'menu':
      return 'accent';
    case 'api':
      return 'gold';
    case 'button':
      return 'outline';
    case 'data':
      return 'moss';
    default:
      return 'default';
  }
}

function expandAll() {
  expanded.value = new Set(allRows.value.filter((r) => r.hasChildren).map((r) => r.node.id));
}
function collapseAll() {
  expanded.value = new Set();
}
defineExpose({ expandAll, collapseAll });
</script>

<style scoped>
.perm-tree {
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
  overflow: hidden;
}

.perm-tree__empty {
  padding: 8px;
}

.perm-row {
  display: flex;
  gap: 10px;
  align-items: center;
  min-height: 42px;
  padding: 6px 12px 6px 10px;
  border-bottom: 1px solid var(--rule);
}

.perm-row:last-child {
  border-bottom: 0;
}

.perm-row:hover {
  background: var(--paper-2);
}

.perm-row__caret {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transition: transform 0.14s var(--ease), color 0.14s var(--ease);
}

.perm-row__caret--open {
  transform: rotate(90deg);
}

.perm-row__caret:hover {
  color: var(--ink);
}

.perm-row__caret--leaf {
  cursor: default;
}

.perm-check {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.perm-check__mark {
  position: relative;
  width: 18px;
  height: 18px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  transition: border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.perm-check__mark--all,
.perm-check__mark--some {
  border-color: var(--ink);
  background: var(--ink);
}

.perm-check__mark--all::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 5px;
  width: 5px;
  height: 9px;
  border-right: 2px solid var(--paper);
  border-bottom: 2px solid var(--paper);
  transform: rotate(45deg);
}

.perm-check__mark--some::after {
  content: '';
  position: absolute;
  top: 8px;
  left: 4px;
  width: 8px;
  height: 2px;
  background: var(--paper);
}

.perm-row__name {
  flex-shrink: 0;
  max-width: 240px;
  overflow: hidden;
  color: var(--ink);
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.perm-row__type {
  flex-shrink: 0;
}

.perm-row__code,
.perm-row__path {
  flex-shrink: 0;
  overflow: hidden;
  max-width: 220px;
  color: var(--ink-3);
  font-family: var(--mono);
  font-size: var(--font-xs);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.perm-row__path {
  color: var(--muted);
}

.perm-row__method {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 3px;
  background: var(--paper-sunk);
  color: var(--muted);
  font-size: var(--font-xxs);
}

.perm-row__spacer {
  flex: 1;
  min-width: 8px;
}

.perm-row__actions {
  display: flex;
  gap: 2px;
  align-items: center;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.14s var(--ease);
}

.perm-row:hover .perm-row__actions {
  opacity: 1;
}

@media (max-width: 860px) {
  .perm-row__path,
  .perm-row__code {
    display: none;
  }

  .perm-row__actions {
    opacity: 1;
  }
}
</style>
