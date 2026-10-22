<script setup>
/**
 * Tableau générique.
 * columns : [{ key, label }]
 * Personnalisation d'une cellule : <template #cell-statut="{ row }">…</template>
 * Colonne d'actions : ajouter { key: 'actions', label: 'Action' } et le slot #cell-actions.
 */
defineProps({
  columns: { type: Array, required: true },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  emptyMessage: { type: String, default: 'Aucune donnée à afficher.' },
})
</script>

<template>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td :colspan="columns.length" class="text-center text-body-secondary py-4">Chargement…</td>
        </tr>
        <tr v-else-if="!rows.length">
          <td :colspan="columns.length" class="text-center text-body-secondary py-4">{{ emptyMessage }}</td>
        </tr>
        <tr v-for="(row, i) in loading ? [] : rows" :key="row.id ?? i">
          <td v-for="col in columns" :key="col.key">
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">{{ row[col.key] ?? '—' }}</slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
