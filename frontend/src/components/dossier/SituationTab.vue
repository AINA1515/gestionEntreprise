<script setup>
import { useFormatters } from '@/composables/useFormatters'

defineProps({ dossier: { type: Object, required: true } })
const { formatDate, formatMontant } = useFormatters()
</script>

<template>
  <div class="row g-3">
    <div class="col-lg-6"><section class="card h-100"><div class="card-body">
      <h2 class="h5">Poste actuel</h2>
      <dl v-if="dossier.affectationCourante" class="row mb-0">
        <dt class="col-sm-5">Intitulé</dt><dd class="col-sm-7">{{ dossier.affectationCourante.intitule }}</dd>
        <dt class="col-sm-5">Code du poste</dt><dd class="col-sm-7">{{ dossier.affectationCourante.codePoste }}</dd>
        <dt class="col-sm-5">Département</dt><dd class="col-sm-7">{{ dossier.departement }}</dd>
        <dt class="col-sm-5">Depuis le</dt><dd class="col-sm-7">{{ formatDate(dossier.affectationCourante.dateDebut) }}</dd>
      </dl>
      <p v-else class="text-body-secondary mb-0">Aucune affectation en cours.</p>
    </div></section></div>
    <div class="col-lg-6"><section class="card h-100"><div class="card-body">
      <h2 class="h5">Contrat en cours</h2>
      <dl v-if="dossier.contratCourant" class="row mb-0">
        <dt class="col-sm-5">Type</dt><dd class="col-sm-7">{{ dossier.contratCourant.type }}</dd>
        <dt class="col-sm-5">Début</dt><dd class="col-sm-7">{{ formatDate(dossier.contratCourant.dateDebut) }}</dd>
        <dt class="col-sm-5">Fin prévue</dt><dd class="col-sm-7">{{ dossier.contratCourant.dateFin ? formatDate(dossier.contratCourant.dateFin) : 'Durée indéterminée' }}</dd>
        <template v-if="dossier.contratCourant.salaireBrut != null"><dt class="col-sm-5">Salaire brut</dt><dd class="col-sm-7">{{ formatMontant(dossier.contratCourant.salaireBrut) }} {{ dossier.contratCourant.devise }}</dd></template>
      </dl>
      <p v-else class="text-body-secondary mb-0">Aucun contrat en cours.</p>
    </div></section></div>
    <div class="col-12"><section class="card"><div class="card-body">
      <h2 class="h5">Parcours dans l’entreprise</h2>
      <dl class="row mb-0">
        <dt class="col-sm-3">Date d’embauche</dt><dd class="col-sm-9">{{ formatDate(dossier.dateEmbaucheInitiale) }}</dd>
        <template v-if="dossier.dateSortie"><dt class="col-sm-3">Date de sortie</dt><dd class="col-sm-9">{{ formatDate(dossier.dateSortie) }}</dd>
        <dt class="col-sm-3">Motif de sortie</dt><dd class="col-sm-9">{{ dossier.motifSortie || '—' }}</dd></template>
      </dl>
    </div></section></div>
  </div>
</template>
