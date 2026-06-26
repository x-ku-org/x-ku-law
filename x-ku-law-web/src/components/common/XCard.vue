<template>
  <section class="x-card">
    <slot />
  </section>
</template>

<style scoped>
.x-card {
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-card);
}
</style>
