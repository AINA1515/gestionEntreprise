<script setup>
import { computed, onMounted, ref } from 'vue'
import { documentService } from '@/services/documentService'
import { dossierService } from '@/services/dossierService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { usePermissions } from '@/composables/usePermissions'
import { useToast } from '@/composables/useToast'
import { useFormatters } from '@/composables/useFormatters'
import { STATUTS_PIECE, TYPES_DOCUMENT } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'
import DocumentList from '@/components/ui/DocumentList.vue'
import FileUploader from '@/components/ui/FileUploader.vue'

const props = defineProps({ dossier: { type: Object, required: true }, mode: { type: String, default: 'moi' } })
const { can } = usePermissions()
const toast = useToast()
const { formatDate } = useFormatters()
const { loading, error, run } = useAsyncAction()
const { loading: envoi, error: erreurEnvoi, run: runEnvoi } = useAsyncAction()

const documents = ref([])
const etat = ref(null)
const progression = ref(0)
const formulaire = ref(false)
const remplacement = ref(null) // document dont on dépose une nouvelle version
const form = ref({ typeDocument: 'PIECE_IDENTITE', fichier: null, dateEmission: '', dateExpiration: '' })
const refus = ref({ ouvert: false, document: null, motif: '' })

const idDossier = computed(() => (props.mode === 'rh' ? props.dossier.id : null))
const peutDeposer = computed(() => can('DOCUMENT_CONSULTER_TOUS') || (props.mode === 'moi' && can('DOCUMENT_CONSULTER_PERSO')))
const peutValider = computed(() => props.mode === 'rh' && can('DOCUMENT_VALIDER'))
const peutConfidentialite = computed(() => props.mode === 'rh' && can('DOCUMENT_CONFIDENTIALITE_GERER'))

async function charger() {
  const [docs, pieces] = await Promise.all([
    run(() => documentService.lister('EMPLOYE', props.dossier.id)),
    run(() => dossierService.pieces(idDossier.value)),
  ])
  documents.value = docs || []
  etat.value = pieces ?? null
}
onMounted(charger)

function deposerPiece(piece) {
  if (piece.documentId) {
    remplacement.value = documents.value.find((d) => d.id === piece.documentId) || null
  } else { remplacement.value = null }
  form.value = { typeDocument: piece.typeDocument, fichier: null, dateEmission: '', dateExpiration: '' }
  formulaire.value = true
}
function nouvelleVersion(doc) {
  remplacement.value = doc
  form.value = { typeDocument: doc.typeDocument, fichier: null, dateEmission: '', dateExpiration: '' }
  formulaire.value = true
}
function fermer() { formulaire.value = false; remplacement.value = null; progression.value = 0 }

async function envoyer() {
  if (!form.value.fichier) return
  const dates = { dateEmission: form.value.dateEmission, dateExpiration: form.value.dateExpiration }
  const ok = await runEnvoi(() => (remplacement.value
    ? documentService.remplacer(remplacement.value.id, form.value.fichier, (e) => { progression.value = Math.round((e.loaded * 100) / (e.total || 1)) }, dates)
    : documentService.televerser(form.value.fichier, { typeDocument: form.value.typeDocument, entiteRattachement: 'EMPLOYE', entiteId: props.dossier.id,
      ...dates, onUploadProgress: (e) => { progression.value = Math.round((e.loaded * 100) / (e.total || 1)) } })))
  if (!ok) { progression.value = 0; return }
  toast.success('Document déposé : il sera examiné par les Ressources humaines.')
  fermer()
  await charger()
}

async function telecharger(doc) {
  const reponse = await run(() => documentService.telecharger(doc.id))
  if (!reponse) return
  const url = URL.createObjectURL(reponse.data)
  const lien = document.createElement('a')
  lien.href = url
  lien.download = doc.nomFichier
  lien.click()
  URL.revokeObjectURL(url)
}
async function valider(doc) { if (await run(() => documentService.valider(doc.id), { successMessage: 'Pièce validée.' })) await charger() }
function demanderRefus(doc) { refus.value = { ouvert: true, document: doc, motif: '' } }
async function confirmerRefus() {
  const { document: doc, motif } = refus.value
  if (!motif.trim()) { toast.error('Le motif du refus est obligatoire.'); return }
  refus.value.ouvert = false
  if (await run(() => documentService.refuser(doc.id, motif.trim()), { successMessage: 'Pièce refusée.' })) await charger()
}
async function archiver(doc) {
  if (!window.confirm(`Archiver « ${doc.nomFichier} » ?`)) return
  await run(() => documentService.archiver(doc.id), { successMessage: 'Document archivé.' })
  await charger()
}
async function confidentialite(doc, niveau) {
  await run(() => documentService.changerConfidentialite(doc.id, niveau), { successMessage: 'Niveau de confidentialité mis à jour.' })
  await charger()
}
</script>

<template>
  <div class="d-grid gap-3">
    <AlertMessage :message="error" />

    <section class="card"><div class="card-body">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h2 class="h5 mb-0">Pièces obligatoires</h2>
        <span v-if="etat" class="badge" :class="etat.complet ? 'text-bg-success' : 'text-bg-warning'">{{ etat.complet ? 'Dossier complet' : `${etat.manquantes + etat.refusees + etat.expirees} à fournir · ${etat.aValider} à valider` }}</span>
      </div>
      <div v-if="etat" class="table-responsive"><table class="table align-middle mb-0">
        <thead><tr><th>Pièce</th><th>Statut</th><th>Validité</th><th class="text-end">Action</th></tr></thead>
        <tbody>
          <tr v-if="!etat.pieces.length"><td colspan="4" class="text-center text-body-secondary">Aucune pièce obligatoire configurée.</td></tr>
          <tr v-for="p in etat.pieces" :key="p.obligatoireId">
            <td>{{ p.libelle }}</td>
            <td><span class="badge" :class="`text-bg-${STATUTS_PIECE[p.statut].variant}`">{{ STATUTS_PIECE[p.statut].label }}</span>
              <div v-if="p.statut === 'REFUSE' && p.motifRefus" class="small text-danger">{{ p.motifRefus }}</div></td>
            <td class="small">{{ p.dateExpiration ? `Expire le ${formatDate(p.dateExpiration)}` : '—' }}</td>
            <td class="text-end"><button v-if="peutDeposer && ['MANQUANT', 'REFUSE', 'EXPIRE', 'EXPIRE_BIENTOT'].includes(p.statut)" class="btn btn-sm btn-outline-primary" type="button" @click="deposerPiece(p)">{{ p.statut === 'MANQUANT' ? 'Déposer' : 'Nouvelle version' }}</button></td>
          </tr>
        </tbody>
      </table></div>
    </div></section>

    <section v-if="formulaire" class="card border-primary"><div class="card-body">
      <h2 class="h5">{{ remplacement ? `Nouvelle version de « ${remplacement.nomFichier} »` : 'Déposer un document' }}</h2>
      <AlertMessage :message="erreurEnvoi" />
      <form @submit.prevent="envoyer">
        <div class="row g-3 mb-3">
          <div class="col-md-4"><label class="form-label" for="dp-type">Type de document</label>
            <select id="dp-type" v-model="form.typeDocument" class="form-select" :disabled="!!remplacement"><option v-for="(libelle, code) in TYPES_DOCUMENT" :key="code" :value="code">{{ libelle }}</option></select></div>
          <div class="col-md-4"><label class="form-label" for="dp-em">Date d’émission</label><input id="dp-em" v-model="form.dateEmission" type="date" class="form-control" /></div>
          <div class="col-md-4"><label class="form-label" for="dp-ex">Date d’expiration</label><input id="dp-ex" v-model="form.dateExpiration" type="date" class="form-control" /><div class="form-text">Laissez vide s’il n’y a pas d’échéance.</div></div>
        </div>
        <FileUploader v-model="form.fichier" :progress="progression" :disabled="envoi" />
        <div class="mt-3 d-flex gap-2">
          <button class="btn btn-primary" type="submit" :disabled="envoi || !form.fichier">{{ envoi ? 'Envoi…' : 'Déposer' }}</button>
          <button class="btn btn-outline-secondary" type="button" :disabled="envoi" @click="fermer">Annuler</button>
        </div>
      </form>
    </div></section>

    <section class="card"><div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h2 class="h5 mb-0">Tous les documents</h2>
        <button v-if="peutDeposer && !formulaire" class="btn btn-sm btn-primary" type="button" @click="deposerPiece({ typeDocument: 'PIECE_IDENTITE' })">Déposer un document</button>
      </div>
      <DocumentList :documents="documents" :loading="loading" show-validity :can-manage="peutDeposer" :can-validate="peutValider"
        :can-change-confidentiality="peutConfidentialite" @download="telecharger" @replace="nouvelleVersion" @archive="archiver"
        @validate="valider" @refuse="demanderRefus" @confidentiality="confidentialite" />
    </div></section>

    <ConfirmModal v-model="refus.ouvert" v-model:justification="refus.motif" title="Refuser la pièce" confirm-text="Refuser" variant="danger"
      requires-justification message="Le salarié verra ce motif et pourra déposer une nouvelle version." @confirm="confirmerRefus" />
  </div>
</template>
