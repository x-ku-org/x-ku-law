<template>
  <div
    ref="rootRef"
    class="x-select"
    :class="{
      'x-select--open': open,
      'x-select--invalid': invalid,
      'x-select--disabled': disabled
    }"
  >
    <button
      v-bind="$attrs"
      :id="triggerId"
      type="button"
      class="x-select__trigger"
      :class="{ 'x-select__trigger--placeholder': !hasTriggerSelection }"
      role="combobox"
      :disabled="disabled"
      :aria-expanded="open"
      :aria-controls="listboxId"
      :aria-activedescendant="open ? activeOptionId : undefined"
      :aria-invalid="invalid ? 'true' : undefined"
      aria-haspopup="listbox"
      @click="toggle"
      @keydown="onTriggerKeydown"
    >
      <span class="x-select__label">{{ triggerLabel }}</span>
      <span class="x-select__chevron" aria-hidden="true"></span>
    </button>

    <Teleport to="body">
      <Transition name="x-select-pop">
        <div v-if="open" ref="popoverRef" class="x-select__popover" :style="popoverStyle">
          <div
            :id="listboxId"
            ref="listboxRef"
            class="x-select__list"
            role="listbox"
            :aria-labelledby="triggerId"
            tabindex="-1"
          >
            <button
              v-for="(option, index) in displayOptions"
              :id="optionId(index)"
              :key="`${String(option.value)}-${index}`"
              type="button"
              class="x-select__option"
              :class="{
                'x-select__option--active': index === activeIndex,
                'x-select__option--selected': isSelected(option)
              }"
              role="option"
              :aria-selected="isSelected(option)"
              @mouseenter="activeIndex = index"
              @click="selectOption(option)"
            >
              <span class="x-select__option-text">{{ option.label }}</span>
              <span v-if="isSelected(option)" class="x-select__check" aria-hidden="true">✓</span>
            </button>
            <p v-if="!displayOptions.length" class="x-select__empty">暂无可选项</p>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useAttrs, watch } from 'vue';
import { useAnchoredPopover } from '@/composables/useAnchoredPopover';
import type { OptionItem } from '@/types/api';

defineOptions({ inheritAttrs: false });

const props = withDefaults(
  defineProps<{
    modelValue?: string | number | boolean;
    options: OptionItem[];
    placeholder?: string;
    invalid?: boolean;
  }>(),
  {
    modelValue: '',
    placeholder: '请选择',
    invalid: false
  }
);

const emit = defineEmits<{ 'update:modelValue': [value: string] }>();
const attrs = useAttrs();
const rootRef = ref<HTMLElement | null>(null);
const popoverRef = ref<HTMLElement | null>(null);
const listboxRef = ref<HTMLElement | null>(null);
const open = ref(false);
const { popoverStyle, containsPopoverTarget } = useAnchoredPopover({
  open,
  anchorRef: rootRef,
  popoverRef,
  minWidth: 220,
  maxWidth: 420
});
const activeIndex = ref(0);
const searchBuffer = ref('');
const emptySelectionLabel = ref('');
let searchTimer: number | undefined;

const instanceId = `x-select-${Math.random().toString(36).slice(2, 9)}`;
const triggerId = `${instanceId}-trigger`;
const listboxId = `${instanceId}-listbox`;
const stringValue = computed(() => String(props.modelValue ?? ''));
const disabled = computed(() => attrs.disabled === true || attrs.disabled === '' || attrs.disabled === 'true');
const selectedOption = computed(() =>
  stringValue.value ? props.options.find((option) => String(option.value) === stringValue.value) : undefined
);
const displayOptions = computed(() => props.options);
const hasTriggerSelection = computed(() => Boolean(selectedOption.value || emptySelectionLabel.value));
const triggerLabel = computed(() => selectedOption.value?.label || emptySelectionLabel.value || props.placeholder);
const activeOptionId = computed(() => (displayOptions.value.length ? optionId(activeIndex.value) : undefined));

watch(stringValue, (value) => {
  if (value) emptySelectionLabel.value = '';
});

watch(open, async (value) => {
  if (!value) return;
  const selectedIndex = displayOptions.value.findIndex((option) => isSelected(option));
  activeIndex.value = selectedIndex >= 0 ? selectedIndex : 0;
  await nextTick();
  scrollActiveIntoView();
});

function optionId(index: number) {
  return `${instanceId}-option-${index}`;
}

function isSelected(option: OptionItem) {
  return String(option.value) === stringValue.value;
}

function toggle() {
  if (disabled.value) return;
  if (open.value) {
    close();
  } else {
    openSelect();
  }
}

function openSelect() {
  if (disabled.value) return;
  open.value = true;
}

function close() {
  open.value = false;
}

function selectOption(option: OptionItem) {
  emptySelectionLabel.value = String(option.value) === '' ? option.label : '';
  emit('update:modelValue', String(option.value));
  close();
}

function moveActive(step: number) {
  if (!displayOptions.value.length) return;
  const next = activeIndex.value + step;
  activeIndex.value = (next + displayOptions.value.length) % displayOptions.value.length;
  scrollActiveIntoView();
}

function onTriggerKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault();
    openSelect();
    moveActive(event.key === 'ArrowDown' ? 1 : -1);
    return;
  }

  if (event.key === 'Home' || event.key === 'End') {
    event.preventDefault();
    openSelect();
    activeIndex.value = event.key === 'Home' ? 0 : Math.max(displayOptions.value.length - 1, 0);
    scrollActiveIntoView();
    return;
  }

  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    if (!open.value) {
      openSelect();
      return;
    }
    const option = displayOptions.value[activeIndex.value];
    if (option) selectOption(option);
    return;
  }

  if (event.key === 'Escape') {
    close();
    return;
  }

  if (event.key.length === 1 && !event.metaKey && !event.ctrlKey && !event.altKey) {
    openSelect();
    typeAhead(event.key);
  }
}

function typeAhead(char: string) {
  searchBuffer.value += char.toLowerCase();
  window.clearTimeout(searchTimer);
  searchTimer = window.setTimeout(() => {
    searchBuffer.value = '';
  }, 650);
  const index = displayOptions.value.findIndex((option) => option.label.toLowerCase().includes(searchBuffer.value));
  if (index >= 0) {
    activeIndex.value = index;
    scrollActiveIntoView();
  }
}

function scrollActiveIntoView() {
  requestAnimationFrame(() => {
    const active = listboxRef.value?.querySelector<HTMLElement>(`#${optionId(activeIndex.value)}`);
    active?.scrollIntoView({ block: 'nearest' });
  });
}

function onDocumentPointerDown(event: PointerEvent) {
  if (!containsPopoverTarget(event.target as Node)) close();
}

onMounted(() => {
  document.addEventListener('pointerdown', onDocumentPointerDown);
});

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onDocumentPointerDown);
  window.clearTimeout(searchTimer);
});
</script>

<style scoped>
.x-select {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
}

.x-select__trigger {
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
  outline: 0;
  cursor: pointer;
  transition:
    border-color 0.18s var(--ease),
    background 0.18s var(--ease),
    box-shadow 0.18s var(--ease),
    transform 0.18s var(--ease);
}

.x-select__trigger:hover {
  border-color: var(--muted-2);
  background: var(--paper);
}

.x-select__trigger:focus-visible {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px color-mix(in oklch, var(--accent), transparent 82%);
}

.x-select--open .x-select__trigger {
  border-color: var(--ink);
  box-shadow: 0 12px 28px color-mix(in oklch, var(--ink), transparent 91%);
}

.x-select__trigger:active {
  transform: translateY(1px);
}

.x-select--invalid .x-select__trigger {
  border-color: var(--rose);
}

.x-select__trigger:disabled {
  background: var(--paper-sunk);
  color: var(--muted);
  cursor: not-allowed;
}

.x-select__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.x-select__trigger--placeholder {
  color: var(--ink-3);
}

.x-select__chevron {
  width: 8px;
  height: 8px;
  justify-self: end;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  opacity: 0.72;
  transform: translateY(-2px) rotate(45deg);
  transition: transform 0.18s var(--ease);
}

.x-select--open .x-select__chevron {
  transform: translateY(2px) rotate(225deg);
}

.x-select__popover {
  padding: 5px;
  border: 1px solid color-mix(in oklch, var(--rule-strong), var(--ink) 14%);
  border-radius: calc(var(--radius-control) + 4px);
  background:
    linear-gradient(180deg, color-mix(in oklch, var(--paper-card), white 42%), var(--paper-card)),
    var(--paper-card);
  box-shadow:
    0 20px 44px color-mix(in oklch, var(--ink), transparent 88%),
    0 1px 0 color-mix(in oklch, white, transparent 40%) inset;
}

.x-select__list {
  display: grid;
  gap: 2px;
  max-height: 248px;
  overflow: auto;
  outline: 0;
  scrollbar-width: thin;
}

.x-select__option {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 16px;
  gap: 10px;
  align-items: center;
  min-height: 32px;
  padding: 0 9px;
  border: 0;
  border-radius: calc(var(--radius-control) - 1px);
  background: transparent;
  color: var(--ink-2);
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.x-select__option:hover,
.x-select__option--active {
  background: var(--paper-sunk);
  color: var(--ink);
}

.x-select__option--selected {
  background: var(--accent-soft);
  color: var(--accent-deep);
  font-weight: 600;
}

.x-select__option-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.x-select__check {
  justify-self: end;
  color: var(--accent);
  font-size: 12px;
  line-height: 1;
}

.x-select__empty {
  margin: 0;
  padding: 10px;
  color: var(--muted);
  font-size: 13px;
}

.x-select-pop-enter-active,
.x-select-pop-leave-active {
  transition:
    opacity 0.14s var(--ease),
    transform 0.14s var(--ease);
}

.x-select-pop-enter-from,
.x-select-pop-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}
</style>
