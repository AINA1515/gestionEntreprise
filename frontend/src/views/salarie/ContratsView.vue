<script setup>
import { onMounted, ref } from 'vue'
import { employeService } from '@/services/employeService'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import AppLayout from '@/components/layout/AppLayout.vue'

const items = [
  { to: '/salarie/accueil', label: 'Accueil', icon: 'house', roles: ['SALARIE'] },
  { to: '/salarie/contrats', label: 'Mes contrats', icon: 'file-earmark-text', roles: ['SALARIE'], permission: 'DOCUMENT_CONSULTER_PERSO' },
  { to: '/taches', label: 'Mes tâches', icon: 'check2-square', roles: ['SALARIE'], permission: 'TACHE_TRAITER' },
  { to: '/offres', label: 'Offres d’emploi', icon: 'search' },
]

const employe = ref(null)
const loading = ref(false)
const error = ref('')
const { formatDate, formatMontant } = useFormatters()

onMounted(async () => {
  loading.value = true
  try { employe.value = await employeService.moi() }
  catch (e) { error.value = e.message || 'Impossible de charger vos contrats.' }
  finally { loading.value = false }
})
</script>

<template>
  <AppLayout espace="Espace salarié" :items="items"><div class="container-fluid py-3">
    <PageHeader title="Mes contrats" :breadcrumb="['Espace salarié', 'Contrats']" subtitle="Consultez vos contrats successifs et leur historique." />
    <AlertMessage :message="error" />
    <section class="card shadow-sm">
      <div v-if="loading" class="card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2"/>Chargement…</div>
      <div v-else-if="!employe?.contrats?.length" class="card-body text-center text-body-secondary py-5">Aucun contrat n’est enregistré.</div>
      <div v-else class="list-group list-group-flush">
        <article v-for="contrat in employe.contrats" :key="contrat.id" class="list-group-item p-4">
          <div class="d-flex flex-wrap justify-content-between gap-2"><h2 class="h5 mb-1">{{ contrat.type }} · {{ contrat.acte?.replaceAll('_', ' ') }}</h2><span class="badge text-bg-light">{{ contrat.statut.replaceAll('_', ' ') }}</span></div>
          <p class="text-body-secondary mb-2">Du {{ formatDate(contrat.dateDebut) }}<span v-if="contrat.dateFin"> au {{ formatDate(contrat.dateFin) }}</span></p>
          <p v-if="contrat.dateFinEffective" class="small text-body-secondary mb-2">Fin effective : {{ formatDate(contrat.dateFinEffective) }}</p>
          <p v-if="contrat.contratPrecedentId" class="small text-body-secondary mb-2">Succède au contrat #{{ contrat.contratPrecedentId }}</p>
          <p v-if="contrat.salaireBrut != null" class="mb-0">Salaire brut : {{ formatMontant(contrat.salaireBrut) }} {{ contrat.devise }}</p>
        </article>
      </div>
    </section>
  </div></AppLayout>
</template>
