<template>
  <div class="sk-table-wrap">
    <table class="sk-table">
      <thead>
        <tr>
          <th v-for="n in columns" :key="n">
            <span class="sk-th sk-shimmer" />
          </th>
          <th v-if="showActions" class="sk-actions-head">
            <span class="sk-th sk-th--short sk-shimmer" />
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row">
          <td v-for="col in columns" :key="col">
            <span class="sk-td sk-shimmer" :class="{ 'sk-td--short': col === columns && row % 2 === 0 }" />
          </td>
          <td v-if="showActions">
            <span class="sk-td sk-td--action sk-shimmer" />
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    columns?: number;
    rows?: number;
    showActions?: boolean;
  }>(),
  {
    columns: 5,
    rows: 8,
    showActions: true
  }
);
</script>

<style scoped>
.sk-table-wrap {
  border: 1px solid var(--rule);
  border-radius: 4px;
  overflow: hidden;
}

.sk-table {
  width: 100%;
  border-collapse: collapse;
}

.sk-table th,
.sk-table td {
  padding: 12px 14px;
  border-bottom: 1px solid var(--rule);
  text-align: left;
}

.sk-table th {
  background: var(--paper-2);
}

.sk-th,
.sk-td {
  display: block;
  height: 12px;
  border-radius: 3px;
}

.sk-th {
  width: 72%;
  max-width: 120px;
}

.sk-th--short {
  width: 48px;
}

.sk-td {
  width: 85%;
}

.sk-td--short {
  width: 55%;
}

.sk-td--action {
  width: 64px;
}

.sk-actions-head {
  width: 100px;
}
</style>
