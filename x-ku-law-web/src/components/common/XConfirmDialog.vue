<template>
  <XModal :open="state.open" kicker="§ Confirm" :title="state.title" :description="state.message" max-width="520px" @update:open="cancel()">
    <template #footer>
      <XButton type="button" variant="ghost" @click="cancel()">{{ state.cancelText }}</XButton>
      <XButton type="button" :variant="state.danger ? 'danger' : 'primary'" @click="accept()">
        {{ state.confirmText }}
      </XButton>
    </template>
  </XModal>
</template>

<script setup lang="ts">
import { useConfirm } from '@/composables/useConfirm';
import XButton from '@/components/common/XButton.vue';
import XModal from '@/components/common/XModal.vue';

const { state, accept, cancel } = useConfirm();
</script>
