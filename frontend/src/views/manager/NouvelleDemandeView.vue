<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useReferentielStore } from '@/stores/referentiel'
import { useEmployesStore } from '@/stores/employes'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormErrors } from '@/composables/useFormErrors'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const demandes = useDemandesStore()
const referentiel = useReferentielStore()
const employes = useEmployesStore()
const chargement = useAsyncAction()
const { fromApiError } = useFormErrors()
const modification = computed(() => Boolean(route.params.id))
const fieldErrors = computed(() => fromApiError({ details: demandes.errorDetails, message: demandes.error }))

const form = reactive({
  departementId: '', posteId: '', typeDemande: 'CREATION', employeRemplaceId: null,
  effectif: 1, motif: '', budget: 0, dateSouhaitee: '',
})

onMounted(async () => {
  await Promise.all([
    chargement.run(() => referentiel.chargerDepartements()),
    employes.chargerCollaborateurs({ size: 100 }).catch(() => {}),
  ])
  if (modification.value) {
    const detail = await demandes.obtenir(route.params.id)
    const demande = detail?.demande
    if (!demande) return
    if (demande.statut !== 'BROUILLON') {
      router.replace({ name: 'manager-demande-detail', params: { id: demande.id } })
      return
    }
    Object.assign(form, {
      departementId: demande.departementId ?? '', posteId: demande.posteId ?? '',
      typeDemande: demande.typeDemande, employeRemplaceId: demande.employeRemplaceId,
      effectif: demande.effectif, motif: demande.motif, budget: demande.budget ?? 0,
      dateSouhaitee: demande.dateSouhaitee,
    })
  }
})

watch(() => form.departementId, (id) => {
  if (id) chargement.run(() => referentiel.chargerPostes(id))
})
watch(() => form.typeDemande, (type) => {
  if (type === 'CREATION') form.employeRemplaceId = null
})

async function enregistrer(soumettre = false) {
  if (soumettre && !window.confirm('Soumettre cette demande à l’équipe RH ? Vous ne pourrez plus la modifier.')) return
  const payload = {
    posteId: Number(form.posteId), typeDemande: form.typeDemande,
    employeRemplaceId: form.employeRemplaceId ? Number(form.employeRemplaceId) : null,
    effectif: form.effectif, motif: form.motif, budget: form.budget,
    dateSouhaitee: form.dateSouhaitee,
  }
  if (modification.value) {
    if (!await demandes.modifier(route.params.id, payload)) return
    if (soumettre && !await demandes.soumettre(route.params.id)) return
    router.push({ name: 'manager-demande-detail', params: { id: route.params.id } })
    return
  }
  const created = await demandes.creer({ ...payload, soumettreDirectement: soumettre })
  if (created) router.push({ name: 'manager-demande-detail', params: { id: created.id } })
}
</script>

<template>
  <div class="col-xl-8">
    <PageHeader :title="modification ? 'Modifier la demande' : 'Nouvelle demande de personnel'"
      :breadcrumb="['Espace Manager', 'Mes demandes', modification ? 'Modification' : 'Nouvelle demande']"
      subtitle="Renseignez les informations nécessaires à votre demande." />

    <AlertMessage :message="chargement.error" />
    <AlertMessage :message="demandes.error" />
    <AlertMessage :message="employes.page?.content?.length === 0 && form.typeDemande === 'REMPLACEMENT' ? 'Aucun collaborateur actif à sélectionner.' : ''" />

    <form class="card shadow-sm" @submit.prevent="enregistrer(false)">
      <div class="card-body p-4">
        <FormField id="departement" label="Département" :error="fieldErrors.departementId">
          <select id="departement" v-model="form.departementId" class="form-select" :class="{ 'is-invalid': fieldErrors.departementId }" required>
            <option value="" disabled>-- Choisir un département --</option>
            <option v-for="d in referentiel.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option>
          </select>
        </FormField>

        <FormField id="poste" label="Poste demandé" :error="fieldErrors.posteId">
          <select id="poste" v-model="form.posteId" class="form-select" :class="{ 'is-invalid': fieldErrors.posteId }" required :disabled="!form.departementId">
            <option value="" disabled>-- Choisir un poste --</option>
            <option v-for="p in referentiel.postes" :key="p.id" :value="p.id">{{ p.intitule }}</option>
          </select>
        </FormField>

        <FormField id="typeDemande" label="Type de demande" :error="fieldErrors.typeDemande">
          <select id="typeDemande" v-model="form.typeDemande" class="form-select" required>
            <option value="CREATION">Création</option>
            <option value="REMPLACEMENT">Remplacement</option>
          </select>
        </FormField>

        <FormField v-if="form.typeDemande === 'REMPLACEMENT'" id="employeRemplaceId" label="Collaborateur remplacé" :error="fieldErrors.employeRemplaceId">
          <select id="employeRemplaceId" v-model="form.employeRemplaceId" class="form-select" required>
            <option :value="null" disabled>-- Sélectionner un collaborateur --</option>
            <option v-for="e in employes.collaborateurs" :key="e.id" :value="e.id">{{ e.prenom }} {{ e.nom }} · {{ e.matricule }}</option>
          </select>
        </FormField>

        <FormField id="effectif" label="Effectif demandé" :error="fieldErrors.effectif">
          <input id="effectif" v-model.number="form.effectif" type="number" min="1" max="999" class="form-control" :class="{ 'is-invalid': fieldErrors.effectif }" required />
        </FormField>
        <FormField id="motif" label="Motif de la demande" :error="fieldErrors.motif">
          <textarea id="motif" v-model="form.motif" rows="4" class="form-control" :class="{ 'is-invalid': fieldErrors.motif }" required></textarea>
        </FormField>
        <FormField id="budget" label="Budget estimé" :error="fieldErrors.budget">
          <input id="budget" v-model.number="form.budget" type="number" min="0" step="0.01" class="form-control" :class="{ 'is-invalid': fieldErrors.budget }" />
        </FormField>
        <FormField id="dateSouhaitee" label="Date souhaitée" :error="fieldErrors.dateSouhaitee">
          <input id="dateSouhaitee" v-model="form.dateSouhaitee" type="date" class="form-control" :class="{ 'is-invalid': fieldErrors.dateSouhaitee }" required />
        </FormField>

        <div class="d-flex flex-wrap gap-2">
          <button type="submit" class="btn btn-outline-primary" :disabled="demandes.loading">Enregistrer en brouillon</button>
          <button type="button" class="btn btn-primary" :disabled="demandes.loading" @click="enregistrer(true)">Soumettre à la RH</button>
          <RouterLink :to="modification ? { name: 'manager-demande-detail', params: { id: route.params.id } } : { name: 'manager-dashboard' }" class="btn btn-link">Annuler</RouterLink>
        </div>
      </div>
    </form>
  </div>
</template>
