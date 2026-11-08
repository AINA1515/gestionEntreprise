<script setup>
import { VALIDATIONS, TYPES_DOCUMENT, NIVEAUX_CONFIDENTIALITE } from '@/composables/useDossierLabels'

defineProps({
  documents: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  canDownload: { type: Boolean, default: true },
  canManage: { type: Boolean, default: false },
  canValidate: { type: Boolean, default: false },
  canChangeConfidentiality: { type: Boolean, default: false },
  showValidity: { type: Boolean, default: false },
})
defineEmits(['download', 'replace', 'archive', 'validate', 'refuse', 'confidentiality'])
const dates = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' })
const jour = (value) => (value ? dates.format(new Date(value)) : '—')
const expire = (d) => d.dateExpiration && new Date(d.dateExpiration) < new Date(new Date().toDateString())
</script>

<template>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>Document</th><th>Type</th><th>Version</th><th>Ajouté le</th>
          <template v-if="showValidity"><th>Validité</th><th>Validation</th><th>Confidentialité</th></template>
          <th class="text-end">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading"><td :colspan="showValidity ? 8 : 5" class="text-center text-body-secondary">Chargement…</td></tr>
        <tr v-else-if="!documents.length"><td :colspan="showValidity ? 8 : 5" class="text-center text-body-secondary">Aucun document.</td></tr>
        <tr v-for="document in documents" v-else :key="document.id" :class="{ 'text-body-secondary': document.statutDocument !== 'ACTIF' }">
          <td>{{ document.nomFichier }}<span v-if="document.statutDocument !== 'ACTIF'" class="badge text-bg-light ms-2">archivé</span></td>
          <td>{{ TYPES_DOCUMENT[document.typeDocument] || document.typeDocument }}</td><td>v{{ document.numeroVersion }}</td>
          <td>{{ jour(document.dateCreation) }}</td>
          <template v-if="showValidity">
            <td>
              <div v-if="document.dateExpiration" :class="{ 'text-danger': expire(document) }">Expire le {{ jour(document.dateExpiration) }}</div>
              <div v-else class="small text-body-secondary">Sans échéance</div>
              <div v-if="document.dateEmission" class="small text-body-secondary">Émis le {{ jour(document.dateEmission) }}</div>
            </td>
            <td>
              <span class="badge" :class="`text-bg-${VALIDATIONS[document.statutValidation]?.variant || 'light'}`">{{ VALIDATIONS[document.statutValidation]?.label || document.statutValidation }}</span>
              <div v-if="document.statutValidation === 'REFUSE' && document.motifRefus" class="small text-danger mt-1">{{ document.motifRefus }}</div>
            </td>
            <td>
              <select v-if="canChangeConfidentiality && document.statutDocument === 'ACTIF'" class="form-select form-select-sm" :value="document.confidentialite"
                :aria-label="`Confidentialité de ${document.nomFichier}`" @change="$emit('confidentiality', document, $event.target.value)">
                <option v-for="(label, code) in NIVEAUX_CONFIDENTIALITE" :key="code" :value="code">{{ label }}</option>
              </select>
              <span v-else>{{ NIVEAUX_CONFIDENTIALITE[document.confidentialite] || document.confidentialite }}</span>
            </td>
          </template>
          <td class="text-end text-nowrap">
            <button v-if="canDownload" class="btn btn-sm btn-outline-primary me-1" type="button" :aria-label="`Télécharger ${document.nomFichier}`" @click="$emit('download', document)"><i class="bi bi-download" aria-hidden="true"></i></button>
            <template v-if="canValidate && document.statutDocument === 'ACTIF' && document.statutValidation === 'A_VALIDER'">
              <button class="btn btn-sm btn-success me-1" type="button" @click="$emit('validate', document)">Valider</button>
              <button class="btn btn-sm btn-outline-danger me-1" type="button" @click="$emit('refuse', document)">Refuser</button>
            </template>
            <button v-if="canManage && document.statutDocument === 'ACTIF'" class="btn btn-sm btn-outline-secondary me-1" type="button" @click="$emit('replace', document)">Remplacer</button>
            <button v-if="canManage && document.statutDocument === 'ACTIF'" class="btn btn-sm btn-outline-danger" type="button" @click="$emit('archive', document)">Archiver</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
