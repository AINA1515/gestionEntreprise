<script setup>
import { onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useReferentielStore } from '@/stores/referentiel'
import { useAsyncAction } from '@/composables/useAsyncAction'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const router = useRouter()
const demandes = useDemandesStore()
const referentiel = useReferentielStore()
const chargement = useAsyncAction()

const form = reactive({
  departementId: '',
  posteId: '',
  effectif: 1,
  motif: '',
  budget: 0,
  dateSouhaitee: '',
})

onMounted(() => chargement.run(() => referentiel.chargerDepartements()))

// Les postes proposés dépendent du département choisi.
watch(() => form.departementId, (id) => {
  form.posteId = ''
  if (id) chargement.run(() => referentiel.chargerPostes(id))
})

async function enregistrer() {
  const { departementId, ...demande } = form
  if (await demandes.creer(demande)) router.push({ name: 'manager-demandes' })
}
</script>

<template>
  <div class="col-xl-8">
    <PageHeader title="Nouvelle demande de personnel" :breadcrumb="['Espace Manager', 'Nouvelle demande']"
                subtitle="Renseignez les informations nécessaires à votre demande." />

    <AlertMessage :message="chargement.error.value" />
    <AlertMessage :message="demandes.error" />

    <form class="card shadow-sm" @submit.prevent="enregistrer">
      <div class="card-body p-4">
        <FormField id="departement" label="Département">
          <select id="departement" v-model="form.departementId" class="form-select" required>
            <option value="" disabled>-- Choisir un département --</option>
            <option v-for="d in referentiel.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option>
          </select>
        </FormField>

        <FormField id="poste" label="Poste demandé">
          <select id="poste" v-model="form.posteId" class="form-select" required :disabled="!form.departementId">
            <option value="" disabled>-- Choisir un poste --</option>
            <option v-for="p in referentiel.postes" :key="p.id" :value="p.id">{{ p.intitule }}</option>
          </select>
        </FormField>

        <FormField id="effectif" label="Effectif demandé">
          <input id="effectif" v-model.number="form.effectif" type="number" min="1" class="form-control" required />
        </FormField>

        <FormField id="motif" label="Motif de la demande">
          <textarea id="motif" v-model="form.motif" rows="4" class="form-control" required></textarea>
        </FormField>

        <FormField id="budget" label="Budget estimé">
          <input id="budget" v-model.number="form.budget" type="number" min="0" step="0.01" class="form-control" required />
        </FormField>

        <FormField id="dateSouhaitee" label="Date souhaitée">
          <input id="dateSouhaitee" v-model="form.dateSouhaitee" type="date" class="form-control" required />
        </FormField>

        <div class="d-flex align-items-center gap-3">
          <button type="submit" class="btn btn-primary" :disabled="demandes.loading">
            {{ demandes.loading ? 'Enregistrement…' : 'Enregistrer la demande' }}
          </button>
          <RouterLink :to="{ name: 'manager-dashboard' }" class="btn btn-link">Annuler</RouterLink>
        </div>
      </div>
    </form>
  </div>
</template>
