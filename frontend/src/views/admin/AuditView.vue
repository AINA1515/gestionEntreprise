<script setup>
import { onMounted, ref } from 'vue'
import { auditService } from '@/services/auditService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { ACTIONS_AUDIT, libelleChamp } from '@/composables/useDossierLabels'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const { loading, error, run } = useAsyncAction()
const filtres = ref({ employeId: '', entite: '', login: '', action: '', du: '', au: '' })
const page = ref(null)
const numero = ref(0)
const heure = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'medium' })
const ENTITES = ['EMPLOYE', 'CONTRAT', 'AFFECTATION', 'DOCUMENT', 'EVOLUTION_SALAIRE', 'UTILISATEUR', 'UTILISATEUR_ROLE', 'ROLE_PERMISSION']

async function charger() { page.value = (await run(() => auditService.rechercher({ ...filtres.value, page: numero.value, size: 50 }))) ?? page.value }
function rechercher() { numero.value = 0; charger() }
function reinitialiser() { filtres.value = { employeId: '', entite: '', login: '', action: '', du: '', au: '' }; rechercher() }
function aller(n) { numero.value = n; charger() }
onMounted(charger)
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Journal d’audit" subtitle="Qui a modifié ou consulté quoi, et quand. Les valeurs sensibles sont masquées selon vos habilitations." :breadcrumb="['Administration', 'Journal d’audit']" />
    <form class="card card-body mb-3" @submit.prevent="rechercher">
      <div class="row g-2 align-items-end">
        <div class="col-6 col-md-2"><label class="form-label" for="au-emp">N° de salarié</label><input id="au-emp" v-model="filtres.employeId" type="number" min="1" class="form-control" /></div>
        <div class="col-6 col-md-2"><label class="form-label" for="au-ent">Élément</label><select id="au-ent" v-model="filtres.entite" class="form-select"><option value="">Tous</option><option v-for="e in ENTITES" :key="e" :value="e">{{ e }}</option></select></div>
        <div class="col-6 col-md-2"><label class="form-label" for="au-act">Action</label><select id="au-act" v-model="filtres.action" class="form-select"><option value="">Toutes</option><option v-for="(l, c) in ACTIONS_AUDIT" :key="c" :value="c">{{ l }}</option></select></div>
        <div class="col-6 col-md-2"><label class="form-label" for="au-log">Auteur</label><input id="au-log" v-model="filtres.login" class="form-control" placeholder="login" /></div>
        <div class="col-6 col-md-2"><label class="form-label" for="au-du">Du</label><input id="au-du" v-model="filtres.du" type="date" class="form-control" /></div>
        <div class="col-6 col-md-2"><label class="form-label" for="au-au">Au</label><input id="au-au" v-model="filtres.au" type="date" class="form-control" /></div>
      </div>
      <div class="mt-3 d-flex gap-2"><button class="btn btn-primary" type="submit" :disabled="loading">Rechercher</button><button class="btn btn-outline-secondary" type="button" @click="reinitialiser">Réinitialiser</button></div>
    </form>
    <AlertMessage :message="error" />
    <div class="card"><div class="table-responsive">
      <table class="table table-sm align-middle mb-0">
        <thead><tr><th>Date</th><th>Auteur</th><th>Action</th><th>Élément</th><th>Salarié</th><th>Champ</th><th>Ancienne valeur</th><th>Nouvelle valeur</th><th>IP</th></tr></thead>
        <tbody>
          <tr v-if="loading && !page"><td colspan="9" class="text-center text-body-secondary">Chargement…</td></tr>
          <tr v-else-if="page && !page.content.length"><td colspan="9" class="text-center text-body-secondary">Aucun événement.</td></tr>
          <tr v-for="l in page?.content" :key="l.id">
            <td class="text-nowrap">{{ heure.format(new Date(l.dateEvenement)) }}</td><td>{{ l.login }}</td><td>{{ ACTIONS_AUDIT[l.action] || l.action }}</td>
            <td>{{ l.entite }}<span v-if="l.entiteId"> #{{ l.entiteId }}</span></td><td>{{ l.employeId ?? '' }}</td>
            <td>{{ l.champ ? libelleChamp(l.champ) : '' }}<div v-if="l.motif" class="small text-body-secondary">{{ l.motif }}</div></td>
            <td>{{ l.ancienneValeur ?? '' }}</td><td>{{ l.nouvelleValeur ?? '' }}</td><td class="small text-body-secondary">{{ l.adresseIp }}</td>
          </tr>
        </tbody>
      </table>
    </div></div>
    <div v-if="page && page.totalPages > 1" class="d-flex justify-content-between align-items-center mt-3">
      <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero === 0" @click="aller(numero - 1)">Précédent</button>
      <span class="small text-body-secondary">Page {{ numero + 1 }} / {{ page.totalPages }} · {{ page.totalElements }} événements</span>
      <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero + 1 >= page.totalPages" @click="aller(numero + 1)">Suivant</button>
    </div>
  </div>
</template>
