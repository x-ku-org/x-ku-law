<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="modal-root" @keydown.esc="emit('update:open', false)">
        <div class="modal-backdrop" @click="emit('update:open', false)" />
        <section
          ref="panelRef"
          tabindex="-1"
          class="modal-panel"
          :style="panelStyle"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="title ? titleId : undefined"
        >
          <header v-if="title || $slots.header" class="modal-head hairline">
            <div class="modal-head-main">
              <slot name="header">
                <div>
                  <div v-if="kicker" class="section-kicker">{{ kicker }}</div>
                  <h2 v-if="title" :id="titleId" class="modal-title">{{ title }}</h2>
                  <p v-if="description" class="modal-description">{{ description }}</p>
                </div>
              </slot>
            </div>
            <button type="button" class="modal-close" aria-label="关闭弹窗" @click="emit('update:open', false)">×</button>
          </header>

          <div v-if="$slots.default" class="modal-body">
            <slot />
          </div>

          <footer v-if="$slots.footer" class="modal-footer hairline">
            <slot name="footer" />
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    open: boolean;
    title?: string;
    description?: string;
    kicker?: string;
    maxWidth?: string;
  }>(),
  {
    title: '',
    description: '',
    kicker: '',
    maxWidth: '720px'
  }
);

const emit = defineEmits<{
  'update:open': [value: boolean];
}>();

const panelRef = ref<HTMLElement | null>(null);
const titleId = `modal-title-${Math.random().toString(36).slice(2, 9)}`;
const panelStyle = computed(() => ({ '--modal-max-width': props.maxWidth }));

watch(
  () => props.open,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : '';
    if (open) {
      window.setTimeout(() => panelRef.value?.focus(), 0);
    }
  }
);

onBeforeUnmount(() => {
  document.body.style.overflow = '';
});
</script>

<style scoped>
.modal-root {
  position: fixed;
  inset: 0;
  z-index: 70;
  display: grid;
  place-items: center;
  padding: 24px;
}

.modal-backdrop {
  position: absolute;
  inset: 0;
  background: color-mix(in oklch, var(--ink), transparent 72%);
  backdrop-filter: blur(6px);
}

.modal-panel {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  width: min(var(--modal-max-width), 100%);
  max-height: min(88vh, 860px);
  overflow: hidden;
  border: 1px solid var(--ink);
  border-radius: 4px;
  background: var(--paper);
  outline: 0;
}

.modal-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
  flex-shrink: 0;
  padding: 22px 24px 18px;
}

.modal-head.hairline {
  border-bottom: 1px solid var(--rule);
}

.modal-head-main {
  flex: 1;
  min-width: 0;
}

.modal-title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: clamp(26px, 4vw, 36px);
  font-weight: 400;
  font-style: italic;
  line-height: 1.05;
}

.modal-description {
  max-width: 66ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.65;
}

.modal-body {
  min-width: 0;
  min-height: 0;
  padding: 20px 24px;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.modal-body::-webkit-scrollbar-thumb {
  border-radius: 0;
  background: var(--rule-strong);
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: var(--muted-2);
}

.modal-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
  padding: 14px 24px 20px;
}

.modal-footer.hairline {
  border-top: 1px solid var(--rule);
}

.modal-close {
  display: grid;
  place-items: center;
  width: var(--control-h);
  height: var(--control-h);
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink-2);
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}

.modal-close:hover {
  border-color: var(--ink);
  color: var(--ink);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.18s var(--ease);
}

.modal-enter-active .modal-panel,
.modal-leave-active .modal-panel {
  transition: transform 0.18s var(--ease);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-panel,
.modal-leave-to .modal-panel {
  transform: translateY(6px);
}

@media (max-width: 860px) {
  .modal-root {
    padding: 12px;
  }

  .modal-head,
  .modal-body,
  .modal-footer {
    padding-left: 18px;
    padding-right: 18px;
  }
}
</style>
