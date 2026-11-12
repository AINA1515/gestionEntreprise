<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminService } from '@/services/adminService'
import { useAuthStore } from '@/stores/auth'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useToast } from '@/composables/useToast'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const auth = useAuthStore()
const toast = useToast()
const { loading, error, run } = useAsyncAction()
const { loading: enregistrement, error: erreurAction, run: runAction } = useAsyncAction()

const onglet = ref('comptes')
const utilisateurs = ref([])
const roles = ref([])
const permissions = ref([])
const recherche = ref('')

// ---- comptes
const creation = ref(false)
const nouveau = ref({ login: '', email: '', nomAffichage: '', motDePasse: '', employeId: '', roles: [] })
const edition = ref(null) // { id, roles: [] }
const STATUTS = { ACTIF: { label: 'Actif', variant: 'success' }, VERROUILLE: { label: 'Verrouillé', variant: 'warning' }, DESACTIVE: { label: 'Désactivé', variant: 'secondary' } }
const rolesActifs = computed(() => roles.value.filter((r) => r.actif))
const filtres = computed(() => {
  const q = recherche.value.trim().toLowerCase()
  return q ? utilisateurs.value.filter((u) => [u.login, u.email, u.nomAffichage].some((v) => v?.toLowerCase().includes(q))) : utilisateurs.value
})
const estMoi = (u) => u.id === auth.utilisateur?.id

async function charger() {
  const [u, r, p] = await Promise.all([run(() => adminService.utilisateurs()), run(() => adminService.roles()), run(() => adminService.permissions())])
  utilisateurs.value = u || []; roles.value = r || []; permissions.value = p || []
}
onMounted(charger)

async function creer() {
  const payload = { ...nouveau.value, employeId: nouveau.value.employeId ? Number(nouveau.value.employeId) : null }
  const u = await runAction(() => adminService.creer(payload))
  if (!u) return
  toast.success(`Compte « ${u.login} » créé.`)
  creation.value = false
  nouveau.value = { login: '', email: '', nomAffichage: '', motDePasse: '', employeId: '', roles: [] }
  await charger()
}
function editerRoles(u) { edition.value = { id: u.id, roles: [...u.roles] } }
async function enregistrerRoles() {
  if (!edition.value.roles.length) { toast.error('Un compte doit conserver au moins un rôle.'); return }
  if (!(await runAction(() => adminService.definirRoles(edition.value.id, edition.value.roles)))) return
  toast.success('Rôles mis à jour. L’utilisateur doit se reconnecter pour que ses droits soient pris en compte.')
  edition.value = null
  await charger()
}
async function changerStatut(u, statutCompte) {
  const verbe = statutCompte === 'ACTIF' ? 'réactiver' : 'désactiver'
  if (!window.confirm(`Voulez-vous ${verbe} le compte « ${u.login} » ?`)) return
  if (await runAction(() => adminService.modifier(u.id, { statutCompte }))) { toast.success('Statut du compte mis à jour.'); await charger() }
}
async function reinitialiser(u) {
  const mdp = window.prompt(`Nouveau mot de passe provisoire pour « ${u.login} » (12 caractères minimum) :`)
  if (!mdp) return
  if (mdp.length < 12) { toast.error('Le mot de passe doit comporter au moins 12 caractères.'); return }
  if (await runAction(() => adminService.modifier(u.id, { nouveauMotDePasse: mdp }))) toast.success('Mot de passe réinitialisé.')
}

// ---- rôles et permissions
const roleChoisi = ref('')
const accordees = ref([])
const modules = computed(() => {
  const groupes = {}
  permissions.value.forEach((p) => { (groupes[p.module || 'AUTRE'] ||= []).push(p) })
  return Object.entries(groupes).sort(([a], [b]) => a.localeCompare(b))
})
async function choisirRole() {
  accordees.value = roleChoisi.value ? ((await run(() => adminService.permissionsRole(roleChoisi.value))) || []) : []
}
async function enregistrerPermissions() {
  if (!window.confirm(`Enregistrer les permissions du rôle « ${roleChoisi.value} » ? Elles s’appliquent à tous ses membres à leur prochaine connexion.`)) return
  const maj = await runAction(() => adminService.definirPermissions(roleChoisi.value, accordees.value))
  if (maj) { accordees.value = maj; toast.success('Permissions du rôle mises à jour.') }
}
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Utilisateurs et habilitations" subtitle="Comptes, rôles et permissions. Chaque modification est tracée dans le journal d’audit." :breadcrumb="['Administration', 'Utilisateurs']" />
    <ul class="nav nav-tabs mb-3">
      <li class="nav-item"><button class="nav-link" :class="{ active: onglet === 'comptes' }" type="button" @click="onglet = 'comptes'">Comptes</button></li>
      <li class="nav-item"><button class="nav-link" :class="{ active: onglet === 'roles' }" type="button" @click="onglet = 'roles'">Rôles et permissions</button></li>
    </ul>
    <AlertMessage :message="error" />
    <AlertMessage :message="erreurAction" />

    <template v-if="onglet === 'comptes'">
      <div class="d-flex flex-wrap justify-content-between gap-2 mb-3">
        <input v-model="recherche" class="form-control w-auto" type="search" placeholder="Rechercher (login, nom, e-mail)" aria-label="Rechercher un utilisateur" />
        <button class="btn btn-primary" type="button" @click="creation = !creation">{{ creation ? 'Fermer' : 'Nouvel utilisateur' }}</button>
      </div>

      <form v-if="creation" class="card card-body mb-3" @submit.prevent="creer">
        <div class="row g-3">
          <div class="col-md-3"><label class="form-label" for="u-login">Login</label><input id="u-login" v-model="nouveau.login" class="form-control" maxlength="100" required /></div>
          <div class="col-md-3"><label class="form-label" for="u-nom">Nom affiché</label><input id="u-nom" v-model="nouveau.nomAffichage" class="form-control" required /></div>
          <div class="col-md-3"><label class="form-label" for="u-email">E-mail</label><input id="u-email" v-model="nouveau.email" type="email" class="form-control" maxlength="150" required /></div>
          <div class="col-md-3"><label class="form-label" for="u-emp">N° de salarié (facultatif)</label><input id="u-emp" v-model="nouveau.employeId" type="number" min="1" class="form-control" /></div>
          <div class="col-md-6"><label class="form-label" for="u-mdp">Mot de passe provisoire</label><input id="u-mdp" v-model="nouveau.motDePasse" type="password" class="form-control" minlength="12" maxlength="72" autocomplete="new-password" required /><div class="form-text">12 caractères minimum.</div></div>
          <div class="col-md-6"><span class="form-label d-block">Rôles</span>
            <div class="form-check form-check-inline" v-for="r in rolesActifs" :key="r.code"><input :id="`nr-${r.code}`" v-model="nouveau.roles" class="form-check-input" type="checkbox" :value="r.code" /><label class="form-check-label" :for="`nr-${r.code}`">{{ r.libelle || r.code }}</label></div>
          </div>
        </div>
        <div class="mt-3"><button class="btn btn-primary" type="submit" :disabled="enregistrement || !nouveau.roles.length">{{ enregistrement ? 'Création…' : 'Créer le compte' }}</button></div>
      </form>

      <div class="card"><div class="table-responsive"><table class="table align-middle mb-0">
        <thead><tr><th>Compte</th><th>Rôles</th><th>Statut</th><th class="text-end">Actions</th></tr></thead>
        <tbody>
          <tr v-if="loading && !utilisateurs.length"><td colspan="4" class="text-center text-body-secondary">Chargement…</td></tr>
          <template v-for="u in filtres" :key="u.id">
            <tr>
              <td><strong>{{ u.nomAffichage }}</strong><span v-if="estMoi(u)" class="badge text-bg-info ms-2">vous</span><div class="small text-body-secondary">{{ u.login }} · {{ u.email }}<span v-if="u.employeId"> · salarié #{{ u.employeId }}</span></div></td>
              <td><span v-for="code in u.roles" :key="code" class="badge text-bg-light border me-1">{{ code }}</span></td>
              <td><span class="badge" :class="`text-bg-${STATUTS[u.statutCompte]?.variant || 'light'}`">{{ STATUTS[u.statutCompte]?.label || u.statutCompte }}</span></td>
              <td class="text-end text-nowrap">
                <button class="btn btn-sm btn-outline-primary me-1" type="button" @click="editerRoles(u)">Rôles</button>
                <button class="btn btn-sm btn-outline-secondary me-1" type="button" @click="reinitialiser(u)">Mot de passe</button>
                <button v-if="u.statutCompte !== 'ACTIF'" class="btn btn-sm btn-outline-success" type="button" @click="changerStatut(u, 'ACTIF')">Réactiver</button>
                <button v-else-if="!estMoi(u)" class="btn btn-sm btn-outline-danger" type="button" @click="changerStatut(u, 'DESACTIVE')">Désactiver</button>
              </td>
            </tr>
            <tr v-if="edition?.id === u.id"><td colspan="4" class="bg-body-tertiary">
              <div class="mb-2 fw-semibold">Rôles de {{ u.nomAffichage }}</div>
              <div class="form-check form-check-inline" v-for="r in rolesActifs" :key="r.code"><input :id="`er-${u.id}-${r.code}`" v-model="edition.roles" class="form-check-input" type="checkbox" :value="r.code" /><label class="form-check-label" :for="`er-${u.id}-${r.code}`" :title="r.description">{{ r.libelle || r.code }}</label></div>
              <div class="mt-2 d-flex gap-2"><button class="btn btn-sm btn-primary" type="button" :disabled="enregistrement || !edition.roles.length" @click="enregistrerRoles">Enregistrer</button><button class="btn btn-sm btn-outline-secondary" type="button" @click="edition = null">Annuler</button></div>
            </td></tr>
          </template>
        </tbody>
      </table></div></div>
    </template>

    <section v-else class="card"><div class="card-body">
      <div class="row g-3 align-items-end mb-3">
        <div class="col-md-5"><label class="form-label" for="rp-role">Rôle</label>
          <select id="rp-role" v-model="roleChoisi" class="form-select" @change="choisirRole"><option value="">Choisir un rôle…</option><option v-for="r in roles" :key="r.code" :value="r.code">{{ r.libelle || r.code }} ({{ r.code }})</option></select></div>
        <div v-if="roleChoisi" class="col-md-7 text-md-end"><button class="btn btn-primary" type="button" :disabled="enregistrement" @click="enregistrerPermissions">{{ enregistrement ? 'Enregistrement…' : 'Enregistrer les permissions' }}</button></div>
      </div>
      <p v-if="roleChoisi === 'ADMIN'" class="alert alert-warning py-2">Le rôle ADMIN doit conserver la permission <code>UTILISATEUR_ADMIN</code>, sans quoi plus personne ne pourrait administrer les accès.</p>
      <div v-if="roleChoisi" class="row g-3">
        <div v-for="[module, liste] in modules" :key="module" class="col-md-6 col-xl-4">
          <div class="border rounded p-3 h-100"><h2 class="h6 text-uppercase text-body-secondary">{{ module }}</h2>
            <div v-for="p in liste" :key="p.code" class="form-check"><input :id="`p-${p.code}`" v-model="accordees" class="form-check-input" type="checkbox" :value="p.code" /><label class="form-check-label" :for="`p-${p.code}`">{{ p.libelle }}<div class="small text-body-secondary"><code>{{ p.code }}</code></div></label></div>
          </div>
        </div>
      </div>
      <p v-else class="text-body-secondary mb-0">Choisissez un rôle pour afficher et modifier ses permissions.</p>
    </div></section>
  </div>
</template>
