<script setup>
import { computed, onMounted, ref } from 'vue'
import { auditService } from '@/services/auditService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { ACTIONS_AUDIT, libelleChamp } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ employeId: { type: [Number, String], required: true }, source: { type: String, default: '' } })
const { loading, error, run } = useAsyncAction()
const page = ref(null)
const recherche = ref('')
const actionFiltre = ref('')
const numero = ref(0)
const heure = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'medium' })

const filtres = { identite: ['nom','prenom','civilite','dateNaissance','lieuNaissance','nationalite','sexe','situationFamiliale','numCin','numCnaps'], coordonnees: ['email','telephone','emailPersonnel','telephonePersonnel','adresse','ville','pays'], situation: ['poste','departement','dateDebut','dateFin','statutActivite'], contrats: ['typeContrat','typeActe','statutContrat','dateDebut','dateFin','salaireBrut'], documents: ['dateExpiration','dateEmission','statutValidation','confidentialite'], remuneration: ['salaireBrut','dateEffet'] }
async function charger() { const result = (await run(() => auditService.rechercher({ employeId: props.employeId, page: 0, size: 100 }))) ?? null; if (result && props.source && filtres[props.source]) result.content = result.content.filter((l) => filtres[props.source].includes(l.champ)); page.value = result }
const lignesFiltrees = computed(() => (page.value?.content || []).filter((l) => (!actionFiltre.value || l.action === actionFiltre.value) && (!recherche.value || `${l.login} ${l.champ || ''} ${l.ancienneValeur || ''} ${l.nouvelleValeur || ''}`.toLowerCase().includes(recherche.value.toLowerCase()))))
onMounted(charger)
function aller(n) { numero.value = n; charger() }
</script>

<template>
  <section class="card"><div class="card-body">
    <h2 class="h5">Historique des modifications et consultations</h2>
    <AlertMessage :message="error" />
    <div v-if="page" class="row g-2 mb-3"><div class="col-md-6"><input v-model="recherche" class="form-control" placeholder="Rechercher dans l’historique…" /></div><div class="col-md-3"><select v-model="actionFiltre" class="form-select"><option value="">Toutes les actions</option><option value="MODIFICATION">Modification</option><option value="VALIDATION">Validation</option><option value="CREATION">Création</option><option value="SUPPRESSION">Suppression</option></select></div></div>
    <p v-if="loading && !page" class="text-body-secondary mb-0">Chargement…</p>
    <div v-else-if="page" class="table-responsive">
      <table class="table table-sm align-middle">
        <thead><tr><th>Date</th><th>Auteur</th><th>Action</th><th>Élément</th><th>Champ</th><th>Ancienne valeur</th><th>Nouvelle valeur</th></tr></thead>
        <tbody>
          <tr v-if="!lignesFiltrees.length"><td colspan="7" class="text-center text-body-secondary">Aucun événement correspondant.</td></tr>
          <tr v-for="l in lignesFiltrees" :key="l.id">
            <td class="text-nowrap">{{ heure.format(new Date(l.dateEvenement)) }}</td><td>{{ l.login }}</td><td>{{ ACTIONS_AUDIT[l.action] || l.action }}</td>
            <td>{{ l.entite }}<span v-if="l.entiteId"> #{{ l.entiteId }}</span></td><td>{{ l.champ ? libelleChamp(l.champ) : '' }}<div v-if="l.motif" class="small text-body-secondary">{{ l.motif }}</div></td>
            <td>{{ l.ancienneValeur ?? '' }}</td><td>{{ l.nouvelleValeur ?? '' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="page.totalPages > 1" class="d-flex justify-content-between align-items-center">
        <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero === 0" @click="aller(numero - 1)">Précédent</button>
        <span class="small text-body-secondary">Page {{ numero + 1 }} / {{ page.totalPages }}</span>
        <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero + 1 >= page.totalPages" @click="aller(numero + 1)">Suivant</button>
      </div>
    </div>
  </div></section>
</template>
