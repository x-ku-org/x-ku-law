import { nextTick, onBeforeUnmount, ref, watch, type CSSProperties, type Ref } from 'vue';

interface UseAnchoredPopoverOptions {
  open: Ref<boolean>;
  anchorRef: Ref<HTMLElement | null>;
  popoverRef: Ref<HTMLElement | null>;
  gap?: number;
  minWidth?: number;
  maxWidth?: number;
  zIndex?: number;
}

export function useAnchoredPopover(options: UseAnchoredPopoverOptions) {
  const popoverStyle = ref<CSSProperties>({});
  const gap = options.gap ?? 6;
  const minWidth = options.minWidth ?? 220;
  const maxWidth = options.maxWidth ?? 420;
  const zIndex = options.zIndex ?? 75;
  const viewportPadding = 16;

  async function updatePosition() {
    const anchor = options.anchorRef.value;
    const popover = options.popoverRef.value;
    if (!anchor || !popover) return;

    const rect = anchor.getBoundingClientRect();
    const width = Math.min(Math.max(rect.width, minWidth), maxWidth, window.innerWidth - viewportPadding * 2);

    let left = rect.left;
    if (left + width > window.innerWidth - viewportPadding) {
      left = window.innerWidth - viewportPadding - width;
    }
    left = Math.max(viewportPadding, left);

    const popoverHeight = popover.offsetHeight;
    const spaceBelow = window.innerHeight - rect.bottom - gap;
    const spaceAbove = rect.top - gap;
    const preferBelow = spaceBelow >= popoverHeight || spaceBelow >= spaceAbove;

    let top = preferBelow ? rect.bottom + gap : rect.top - gap - popoverHeight;
    top = Math.max(viewportPadding, Math.min(top, window.innerHeight - viewportPadding - popoverHeight));

    popoverStyle.value = {
      position: 'fixed',
      top: `${top}px`,
      left: `${left}px`,
      width: `${width}px`,
      maxWidth: `${maxWidth}px`,
      zIndex
    };
  }

  function reposition() {
    if (!options.open.value) return;
    void updatePosition();
  }

  watch(options.open, async (value) => {
    if (!value) {
      popoverStyle.value = {};
      return;
    }
    await nextTick();
    await updatePosition();
    requestAnimationFrame(() => {
      void updatePosition();
    });
  });

  watch(
    () => options.popoverRef.value,
    (node) => {
      if (node && options.open.value) {
        void updatePosition();
      }
    }
  );

  onBeforeUnmount(() => {
    window.removeEventListener('scroll', reposition, true);
    window.removeEventListener('resize', reposition);
  });

  watch(options.open, (value) => {
    if (value) {
      window.addEventListener('scroll', reposition, true);
      window.addEventListener('resize', reposition);
    } else {
      window.removeEventListener('scroll', reposition, true);
      window.removeEventListener('resize', reposition);
    }
  });

  function containsPopoverTarget(target: Node | null) {
    if (!target) return false;
    return Boolean(options.anchorRef.value?.contains(target) || options.popoverRef.value?.contains(target));
  }

  return {
    popoverStyle,
    updatePosition,
    containsPopoverTarget
  };
}
