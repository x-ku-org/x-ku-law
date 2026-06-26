import { readonly, ref } from 'vue';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

interface ConfirmState extends Required<Omit<ConfirmOptions, 'danger'>> {
  open: boolean;
  danger: boolean;
}

const state = ref<ConfirmState>({
  open: false,
  title: '',
  message: '',
  confirmText: '确认',
  cancelText: '取消',
  danger: false
});

let resolver: ((value: boolean) => void) | null = null;

function settle(value: boolean) {
  state.value.open = false;
  resolver?.(value);
  resolver = null;
}

export function useConfirm() {
  return {
    state: readonly(state),
    confirm(options: ConfirmOptions) {
      state.value = {
        open: true,
        title: options.title,
        message: options.message,
        confirmText: options.confirmText || '确认',
        cancelText: options.cancelText || '取消',
        danger: Boolean(options.danger)
      };
      return new Promise<boolean>((resolve) => {
        resolver = resolve;
      });
    },
    accept() {
      settle(true);
    },
    cancel() {
      settle(false);
    }
  };
}
