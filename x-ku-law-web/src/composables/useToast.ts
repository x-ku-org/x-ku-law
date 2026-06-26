import { computed, readonly, ref } from 'vue';

export type ToastTone = 'info' | 'success' | 'error';

export interface ToastItem {
  id: number;
  tone: ToastTone;
  message: string;
}

const toasts = ref<ToastItem[]>([]);
let toastId = 0;

function dismiss(id: number) {
  toasts.value = toasts.value.filter((toast) => toast.id !== id);
}

function push(message: string, tone: ToastTone) {
  const trimmed = message.trim();
  if (!trimmed) {
    return;
  }
  const id = ++toastId;
  toasts.value = [...toasts.value, { id, tone, message: trimmed }];
  window.setTimeout(() => dismiss(id), 3200);
}

export function useToast() {
  return {
    toasts: readonly(computed(() => toasts.value)),
    dismiss,
    info(message: string) {
      push(message, 'info');
    },
    success(message: string) {
      push(message, 'success');
    },
    error(message: string) {
      push(message, 'error');
    }
  };
}
