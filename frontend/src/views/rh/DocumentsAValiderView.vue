<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { documentService } from '@/services/documentService'
import { useDocumentsAValiderStore } from '@/stores/documentsAValider'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useToast } from '@/composables/useToast'
import { TYPES_DOCUMENT } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'

const store = useDocumentsAValiderStore()
const route = useRoute()
const toast = useToast()
const { loading: actionLoading, error: actionError, run } = useAsyncAction()
const recherche = ref('')
const type = ref('')
const depuis = ref('')
const jusqua = ref('')
const refus = ref({ ouvert: false, ligne: null, motif: '' })

const types = computed(() => [...new Set(store.documents.map((x) => x.document.typeDocument))].sort())
const lignes = computed(() => store.documents.filter((x) => {
  const d = x.document
  const nom = `${x.candidat || ''} ${x.email || ''} ${x.offre || ''} ${d.nomFichier}`.toLocaleLowerCase()
  const date = d.dateCreation?.slice(0, 10) || ''
  const candidatureId = route.query.candidatureId ? String(route.query.candidatureId) : ''
  const candidatId = route.query.candidatId ? String(route.query.candidatId) : ''
  return (!recherche.value || nom.includes(recherche.value.trim().toLocaleLowerCase()))
    && (!type.value || d.typeDocument === type.value)
    && (!depuis.value || date >= depuis.value)
    && (!jusqua.value || date <= jusqua.value)
    && ((!candidatureId && !candidatId)
      || (candidatureId && String(x.candidatureId || '') === candidatureId)
      || (candidatId && (d.entiteRattachement === 'CANDIDAT' ? String(d.entiteId) === candidatId : String(x.candidatId || '') === candidatId)))
}))

onMounted(store.charger)

function cible(ligne) {
  const d = ligne.document
  if (ligne.candidatureId) return { name: 'candidature-detail', params: { id: ligne.candidatureId } }
  if (d.entiteRattachement === 'CANDIDAT') return { name: 'rh-employe-cv', params: { candidatId: d.entiteId } }
  if (d.entiteRattachement === 'EMPLOYE') return { name: 'rh-employe-dossier', params: { id: d.entiteId }, query: { onglet: 'documents' } }
  return null
}

async function visualiser(ligne) {
  const response = await run(() => documentService.visualiser(ligne.document.id))
  if (!response) return
  const url = URL.createObjectURL(response.data)
  window.open(url, '_blank', 'noopener,noreferrer')
  setTimeout(() => URL.revokeObjectURL(url), 60000)
}

async function valider(ligne) {
  if (await run(() => documentService.valider(ligne.document.id))) {
    toast.success('Document validé.')
    await store.charger()
  }
}

function ouvrirRefus(ligne) { refus.value = { ouvert: true, ligne, motif: '' } }
async function confirmerRefus() {
  const { ligne, motif } = refus.value
  if (!motif.trim()) { toast.error('Le motif du refus est obligatoire.'); return }
  refus.value.ouvert = false
  if (await run(() => documentService.refuser(ligne.document.id, motif.trim()))) {
    toast.success('Document refusé.')
    await store.charger()
  }
}
</script>

<template>
  <div class="container-fluid py-3">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
      <div><h1 class="h3 mb-1">Documents à valider</h1><p class="text-body-secondary mb-0">Pièces envoyées par les candidats et les salariés.</p></div>
      <span class="badge text-bg-warning fs-6">{{ store.nombre }} en attente</span>
    </div>
    <AlertMessage :message="store.error || actionError" />

    <section class="card mb-3"><div class="card-body">
      <div class="row g-2 align-items-end">
        <div class="col-lg-4"><label class="form-label" for="doc-recherche">Candidat, e-mail ou fichier</label><input id="doc-recherche" v-model="recherche" class="form-control" type="search" placeholder="Rechercher…" /></div>
        <div class="col-lg-3"><label class="form-label" for="doc-type">Type de document</label><select id="doc-type" v-model="type" class="form-select"><option value="">Tous les types</option><option v-for="t in types" :key="t" :value="t">{{ TYPES_DOCUMENT[t] || t }}</option></select></div>
        <div class="col-sm-6 col-lg-2"><label class="form-label" for="doc-depuis">Envoyé depuis</label><input id="doc-depuis" v-model="depuis" type="date" class="form-control" /></div>
        <div class="col-sm-6 col-lg-2"><label class="form-label" for="doc-jusqua">Envoyé jusqu’au</label><input id="doc-jusqua" v-model="jusqua" type="date" class="form-control" /></div>
        <div class="col-lg-1"><button class="btn btn-outline-secondary w-100" type="button" title="Actualiser" :disabled="store.loading" @click="store.charger"><i class="bi bi-arrow-clockwise" aria-hidden="true"></i></button></div>
      </div>
    </div></section>

    <section class="card"><div class="table-responsive"><table class="table table-hover align-middle mb-0">
      <thead><tr><th>Personne / dossier</th><th>Document</th><th>Envoyé le</th><th>Source</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
        <tr v-if="store.loading && !store.documents.length"><td colspan="5" class="text-center py-4">Chargement…</td></tr>
        <tr v-else-if="!lignes.length"><td colspan="5" class="text-center text-body-secondary py-4">Aucun document ne correspond aux filtres.</td></tr>
        <tr v-for="ligne in lignes" :key="ligne.document.id">
          <td><RouterLink v-if="cible(ligne)" :to="cible(ligne)" class="fw-semibold">{{ ligne.candidat || `Dossier ${ligne.document.entiteRattachement} #${ligne.document.entiteId}` }}</RouterLink><strong v-else>{{ ligne.candidat || `Dossier ${ligne.document.entiteRattachement} #${ligne.document.entiteId}` }}</strong><div v-if="ligne.email" class="small text-body-secondary">{{ ligne.email }}</div></td>
          <td><strong>{{ TYPES_DOCUMENT[ligne.document.typeDocument] || ligne.document.typeDocument }}</strong><div class="small text-body-secondary">{{ ligne.document.nomFichier }}</div></td>
          <td>{{ ligne.document.dateCreation ? new Date(ligne.document.dateCreation).toLocaleString('fr-FR') : '—' }}</td>
          <td>{{ ligne.offre || (ligne.document.entiteRattachement === 'EMPLOYE' ? 'Dossier salarié' : ligne.document.entiteRattachement === 'CANDIDAT' ? 'Profil candidat' : 'Candidature') }}</td>
          <td><div class="d-flex justify-content-end flex-wrap gap-1"><button class="btn btn-sm btn-outline-secondary" type="button" :disabled="actionLoading" @click="visualiser(ligne)">Voir</button><button class="btn btn-sm btn-success" type="button" :disabled="actionLoading" @click="valider(ligne)">Valider</button><button class="btn btn-sm btn-outline-danger" type="button" :disabled="actionLoading" @click="ouvrirRefus(ligne)">Refuser</button></div></td>
        </tr>
      </tbody>
    </table></div></section>

    <ConfirmModal v-model="refus.ouvert" v-model:justification="refus.motif" title="Refuser le document" confirm-text="Refuser" variant="danger" requires-justification message="Le déposant verra le motif et pourra transmettre une nouvelle version." @confirm="confirmerRefus" />
  </div>
</template>
