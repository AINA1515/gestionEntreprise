<script setup>
defineProps({
  documents: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  canDownload: { type: Boolean, default: true },
  canManage: { type: Boolean, default: false },
})
defineEmits(['download', 'replace', 'archive'])
const dates = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' })
</script>

<template>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead><tr><th>Document</th><th>Type</th><th>Version</th><th>Ajouté le</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
        <tr v-if="loading"><td colspan="5" class="text-center text-body-secondary">Chargement…</td></tr>
        <tr v-else-if="!documents.length"><td colspan="5" class="text-center text-body-secondary">Aucun document.</td></tr>
        <tr v-for="document in documents" v-else :key="document.id">
          <td>{{ document.nomFichier }}</td><td>{{ document.typeDocument }}</td><td>v{{ document.numeroVersion }}</td>
          <td>{{ document.dateCreation ? dates.format(new Date(document.dateCreation)) : '—' }}</td>
          <td class="text-end text-nowrap">
            <button v-if="canDownload" class="btn btn-sm btn-outline-primary me-1" type="button" :aria-label="`Télécharger ${document.nomFichier}`" @click="$emit('download', document)"><i class="bi bi-download" aria-hidden="true"></i></button>
            <button v-if="canManage && document.statutDocument === 'ACTIF'" class="btn btn-sm btn-outline-secondary me-1" type="button" @click="$emit('replace', document)">Remplacer</button>
            <button v-if="canManage && document.statutDocument === 'ACTIF'" class="btn btn-sm btn-outline-danger" type="button" @click="$emit('archive', document)">Archiver</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
