<template>
  <div
    ref="rootRef"
    class="x-relation"
    :class="{ 'x-relation--open': open, 'x-relation--invalid': invalid, 'x-relation--disabled': disabled }"
  >
    <button
      type="button"
      class="x-relation__trigger"
      :class="{ 'x-relation__trigger--placeholder': !hasSelection }"
      :disabled="disabled"
      :aria-expanded="open"
      @click="toggle"
      @keydown="onTriggerKeydown"
    >
      <span class="x-relation__label">{{ triggerLabel }}</span>
      <button
        v-if="hasSelection && !disabled"
        type="button"
        class="x-relation__clear"
        aria-label="清除"
        @click.stop="clearSelection"
      >
        ×
      </button>
      <span v-else class="x-relation__chevron" aria-hidden="true"></span>
    </button>

    <Teleport to="body">
      <Transition name="x-relation-pop">
        <div v-if="open" ref="popoverRef" class="x-relation__popover" :style="popoverStyle">
          <input
            ref="searchRef"
            v-model="keyword"
            class="x-relation__search"
            type="text"
            :placeholder="searchPlaceholder"
            @keydown="onSearchKeydown"
          />
          <div ref="listRef" class="x-relation__list" role="listbox">
            <button
              v-for="(item, index) in items"
              :key="String(item.id)"
              type="button"
              class="x-relation__option"
              :class="{ 'x-relation__option--active': index === activeIndex, 'x-relation__option--selected': String(item.id) === stringValue }"
              role="option"
              @mouseenter="activeIndex = index"
              @click="selectItem(item)"
            >
              <span class="x-relation__option-text">{{ item.label }}</span>
              <span v-if="item.sub" class="x-relation__option-sub">{{ item.sub }}</span>
            </button>
            <p v-if="loading" class="x-relation__hint">搜索中…</p>
            <p v-else-if="!items.length" class="x-relation__hint">
              {{ keyword ? '没有匹配的记录' : '输入关键词以搜索' }}
            </p>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useAnchoredPopover } from '@/composables/useAnchoredPopover';
import { relationSources, type RelationItem } from '@/utils/relationSources';

const props = withDefaults(
  defineProps<{
    modelValue?: string | number | null;
    resource: string;
    placeholder?: string;
    invalid?: boolean;
    disabled?: boolean;
    /** 编辑态回显用：行内已带的冗余名（如 documentTitle），优先于按 id 解析 */
    initialLabel?: string;
    /** 级联查询参数（如 { documentId }），随搜索一起发给后端 */
    queryParams?: Record<string, unknown>;
  }>(),
  { modelValue: '', placeholder: '搜索并选择…', invalid: false, disabled: false, initialLabel: '', queryParams: () => ({}) }
);

const emit = defineEmits<{ 'update:modelValue': [value: string | number | ''] }>();

const source = computed(() => relationSources[props.resource]);
const rootRef = ref<HTMLElement | null>(null);
const popoverRef = ref<HTMLElement | null>(null);
const searchRef = ref<HTMLInputElement | null>(null);
const listRef = ref<HTMLElement | null>(null);
const open = ref(false);
const { popoverStyle, containsPopoverTarget } = useAnchoredPopover({
  open,
  anchorRef: rootRef,
  popoverRef,
  minWidth: 260,
  maxWidth: 440
});
const keyword = ref('');
const items = ref<RelationItem[]>([]);
const loading = ref(false);
const activeIndex = ref(0);
const selectedLabel = ref('');
let debounceTimer: number | undefined;
let reqToken = 0;

const stringValue = computed(() => (props.modelValue === null || props.modelValue === undefined ? '' : String(props.modelValue)));
const hasSelection = computed(() => stringValue.value !== '' && Boolean(selectedLabel.value));
const triggerLabel = computed(() => selectedLabel.value || (stringValue.value ? `#${stringValue.value}` : props.placeholder));
const searchPlaceholder = computed(() => props.placeholder);

watch(
  () => [props.modelValue, props.initialLabel] as const,
  () => resolveLabel(),
  { immediate: true }
);


watch(
  () => JSON.stringify(props.queryParams),
  () => {
    if (open.value) void runSearch();
  }
);

async function resolveLabel() {
  if (stringValue.value === '') {
    selectedLabel.value = '';
    return;
  }
  if (props.initialLabel) {
    selectedLabel.value = props.initialLabel;
    return;
  }
  const src = source.value;
  if (src?.resolve) {
    try {
      const raw = (await src.resolve(stringValue.value)) as Record<string, unknown> | null;
      selectedLabel.value = raw ? src.toItem(raw).label : `#${stringValue.value}`;
      return;
    } catch {
      /* 解析失败回退 #id */
    }
  }
  selectedLabel.value = `#${stringValue.value}`;
}

function toggle() {
  if (props.disabled) return;
  if (open.value) {
    close();
  } else {
    void openPanel();
  }
}

async function openPanel() {
  open.value = true;
  await nextTick();
  searchRef.value?.focus();
  void runSearch();
}

function close() {
  open.value = false;
  keyword.value = '';
}

async function runSearch() {
  const src = source.value;
  if (!src) return;
  loading.value = true;
  const token = ++reqToken;
  try {
    const page = await src.search({ keyword: keyword.value || undefined, pageNo: 1, pageSize: 20, ...props.queryParams });
    if (token !== reqToken) return;
    items.value = (page.list as Record<string, unknown>[]).map((r) => src.toItem(r));
    activeIndex.value = 0;
  } catch {
    if (token === reqToken) items.value = [];
  } finally {
    if (token === reqToken) loading.value = false;
  }
}

watch(keyword, () => {
  window.clearTimeout(debounceTimer);
  debounceTimer = window.setTimeout(() => void runSearch(), 250);
});

function selectItem(item: RelationItem) {
  selectedLabel.value = item.label;
  emit('update:modelValue', item.id);
  close();
}

function clearSelection() {
  selectedLabel.value = '';
  emit('update:modelValue', '');
}

function moveActive(step: number) {
  if (!items.value.length) return;
  activeIndex.value = (activeIndex.value + step + items.value.length) % items.value.length;
  requestAnimationFrame(() => {
    const nodes = listRef.value?.querySelectorAll<HTMLElement>('.x-relation__option');
    nodes?.[activeIndex.value]?.scrollIntoView({ block: 'nearest' });
  });
}

function onTriggerKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowDown' || event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    if (!open.value) void openPanel();
  } else if (event.key === 'Escape') {
    close();
  }
}

function onSearchKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowDown') {
    event.preventDefault();
    moveActive(1);
  } else if (event.key === 'ArrowUp') {
    event.preventDefault();
    moveActive(-1);
  } else if (event.key === 'Enter') {
    event.preventDefault();
    const item = items.value[activeIndex.value];
    if (item) selectItem(item);
  } else if (event.key === 'Escape') {
    event.preventDefault();
    close();
  }
}

function onDocumentPointerDown(event: PointerEvent) {
  if (!containsPopoverTarget(event.target as Node)) close();
}

onMounted(() => document.addEventListener('pointerdown', onDocumentPointerDown));
onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onDocumentPointerDown);
  window.clearTimeout(debounceTimer);
});
</script>

<style scoped>
.x-relation {
  position: relative;
  width: 100%;
  min-width: 0;
}

.x-relation__trigger {
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 16px;
  gap: 10px;
  align-items: center;
  width: 100%;
  height: var(--control-h);
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: linear-gradient(180deg, color-mix(in oklch, var(--paper), white 34%), var(--paper));
  color: var(--ink);
  font: inherit;
  line-height: 1;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s var(--ease), background 0.18s var(--ease), box-shadow 0.18s var(--ease);
}

.x-relation__trigger:hover {
  border-color: var(--muted-2);
  background: var(--paper);
}

.x-relation--open .x-relation__trigger {
  border-color: var(--ink);
  box-shadow: 0 12px 28px color-mix(in oklch, var(--ink), transparent 91%);
}

.x-relation--invalid .x-relation__trigger {
  border-color: var(--rose);
}

.x-relation__trigger:disabled {
  background: var(--paper-sunk);
  color: var(--muted);
  cursor: not-allowed;
}

.x-relation__trigger--placeholder .x-relation__label {
  color: var(--ink-3);
}

.x-relation__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.x-relation__chevron {
  width: 8px;
  height: 8px;
  justify-self: end;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  opacity: 0.72;
  transform: translateY(-2px) rotate(45deg);
}

.x-relation__clear {
  justify-self: end;
  width: 16px;
  height: 16px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--paper-sunk);
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
}

.x-relation__clear:hover {
  background: var(--rule-strong);
  color: var(--ink);
}

.x-relation__popover {
  padding: 6px;
  border: 1px solid color-mix(in oklch, var(--rule-strong), var(--ink) 14%);
  border-radius: calc(var(--radius-control) + 4px);
  background: var(--paper-card);
  box-shadow: 0 20px 44px color-mix(in oklch, var(--ink), transparent 88%);
}

.x-relation__search {
  box-sizing: border-box;
  width: 100%;
  height: var(--control-h-sm);
  margin-bottom: 6px;
  padding: 0 10px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: var(--paper);
  color: var(--ink);
  font: inherit;
  font-size: 13px;
  outline: 0;
}

.x-relation__search:focus {
  border-color: var(--accent);
}

.x-relation__list {
  display: grid;
  gap: 2px;
  max-height: 248px;
  overflow: auto;
  scrollbar-width: thin;
}

.x-relation__option {
  display: grid;
  gap: 1px;
  padding: 6px 9px;
  border: 0;
  border-radius: calc(var(--radius-control) - 1px);
  background: transparent;
  color: var(--ink-2);
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.x-relation__option:hover,
.x-relation__option--active {
  background: var(--paper-sunk);
  color: var(--ink);
}

.x-relation__option--selected {
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.x-relation__option-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.x-relation__option-sub {
  color: var(--muted);
  font-family: var(--mono);
  font-size: var(--font-xxs);
}

.x-relation__hint {
  margin: 0;
  padding: 10px;
  color: var(--muted);
  font-size: 13px;
}

.x-relation-pop-enter-active,
.x-relation-pop-leave-active {
  transition: opacity 0.14s var(--ease), transform 0.14s var(--ease);
}

.x-relation-pop-enter-from,
.x-relation-pop-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}
</style>
