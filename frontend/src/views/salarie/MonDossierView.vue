<script setup>
import PageHeader from '@/components/ui/PageHeader.vue'
import DossierPage from '@/components/dossier/DossierPage.vue'
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Mon dossier" subtitle="Vos informations administratives, vos documents et votre parcours." :breadcrumb="['Espace salarié', 'Mon dossier']" />
    <DossierPage />
  </div>
</template>
