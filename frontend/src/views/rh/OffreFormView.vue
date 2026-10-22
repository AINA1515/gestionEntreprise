<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOffresStore } from '@/stores/offres'
import { useAsyncAction } from '@/composables/useAsyncAction'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const store = useOffresStore()
const { loading, error, run } = useAsyncAction()
const id = computed(() => route.params.id)
const demandeId = computed(() => route.query.demandeId)
const form = reactive({ titre: '', description: '', profilRecherche: '', diffusion: 'EXTERNE', dateLimiteCandidature: '', lieu: '', typeContrat: '', tempsTravail: '' })
const edition = computed(() => !!id.value)

onMounted(async () => {
  if (!edition.value) return
  const offre = await store.obtenir(id.value)
  if (!offre) return
  if (offre.statut !== 'BROUILLON') { router.replace({ name: 'rh-offres' }); return }
  Object.assign(form, { titre: offre.titre || '', description: offre.description || '', profilRecherche: offre.profilRecherche || '',
    diffusion: offre.diffusion || 'EXTERNE', dateLimiteCandidature: offre.dateLimiteCandidature || '', lieu: offre.lieu || '',
    typeContrat: offre.typeContrat || '', tempsTravail: offre.tempsTravail || '' })
})

async function enregistrer() {
  const data = { titre: form.titre || null, description: form.description || null, profilRecherche: form.profilRecherche || null,
    diffusion: form.diffusion, dateLimiteCandidature: form.dateLimiteCandidature || null, lieu: form.lieu || null,
    typeContrat: form.typeContrat || null, tempsTravail: form.tempsTravail || null }
  const result = await run(() => edition.value
    ? store.modifier(id.value, data)
    : store.creer({ ...data, demandeId: Number(demandeId.value) }))
  if (result) router.push({ name: 'rh-offres' })
}
</script>

<template>
  <PageHeader :title="edition ? 'Modifier l’offre' : 'Créer une offre d’emploi'" :breadcrumb="['Espace RH', 'Offres', edition ? 'Modification' : 'Nouvelle offre']"
    :subtitle="edition ? 'Seules les offres en brouillon peuvent être modifiées.' : 'Cette offre sera rattachée à une demande validée.'" />
  <div class="col-xl-9"><AlertMessage :message="error || store.error" />
    <div v-if="!edition && !demandeId" class="alert alert-warning">Une demande validée est nécessaire pour créer une offre. Ouvrez la demande concernée puis choisissez « Créer l’offre d’emploi ».</div>
    <form class="card shadow-sm" @submit.prevent="enregistrer"><div class="card-body p-4">
      <p v-if="demandeId" class="badge text-bg-light border">Demande associée : #{{ demandeId }}</p>
      <FormField id="offre-titre" label="Titre *"><input id="offre-titre" v-model="form.titre" class="form-control" maxlength="250" required /></FormField>
      <FormField id="offre-description" label="Description du poste *"><textarea id="offre-description" v-model="form.description" class="form-control" rows="6" maxlength="4000" required /></FormField>
      <FormField id="offre-profil" label="Profil recherché"><textarea id="offre-profil" v-model="form.profilRecherche" class="form-control" rows="4" maxlength="4000" /></FormField>
      <div class="row"><div class="col-md-6"><FormField id="offre-diffusion" label="Diffusion"><select id="offre-diffusion" v-model="form.diffusion" class="form-select"><option value="EXTERNE">Externe</option><option value="INTERNE">Interne</option><option value="MIXTE">Interne et externe</option></select></FormField></div>
        <div class="col-md-6"><FormField id="offre-date-limite" label="Date limite de candidature"><input id="offre-date-limite" v-model="form.dateLimiteCandidature" type="date" class="form-control" /></FormField></div></div>
      <div class="row"><div class="col-md-6"><FormField id="offre-lieu" label="Lieu"><input id="offre-lieu" v-model="form.lieu" class="form-control" maxlength="200" /></FormField></div>
        <div class="col-md-6"><FormField id="offre-type" label="Type de contrat"><select id="offre-type" v-model="form.typeContrat" class="form-select"><option value="">À préciser</option><option value="CDI">CDI</option><option value="CDD">CDD</option><option value="STAGE">Stage</option><option value="ALTERNANCE">Alternance</option></select></FormField></div></div>
      <FormField id="offre-temps" label="Temps de travail"><select id="offre-temps" v-model="form.tempsTravail" class="form-select"><option value="">À préciser</option><option value="TEMPS_PLEIN">Temps plein</option><option value="TEMPS_PARTIEL">Temps partiel</option></select></FormField>
    </div><div class="card-footer d-flex justify-content-end gap-2"><RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-offres' }">Annuler</RouterLink>
      <button class="btn btn-primary" type="submit" :disabled="loading || store.loading || (!edition && !demandeId)">{{ loading || store.loading ? 'Enregistrement…' : 'Enregistrer en brouillon' }}</button></div></form>
  </div>
</template>
