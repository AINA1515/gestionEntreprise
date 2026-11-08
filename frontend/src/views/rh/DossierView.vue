<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import DossierPage from '@/components/dossier/DossierPage.vue'

const route = useRoute()
const employeId = computed(() => Number(route.params.id))
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Dossier administratif" :breadcrumb="['Espace Ressources humaines', 'Employés', 'Dossier administratif']" />
    <DossierPage :employe-id="employeId">
      <template #actions><RouterLink class="btn btn-sm btn-outline-secondary" :to="{ name: 'rh-employe-detail', params: { id: employeId } }">Fiche salarié</RouterLink></template>
    </DossierPage>
    <div class="mt-4"><RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-employes' }">Retour à la liste des salariés</RouterLink></div>
  </div>
</template>
