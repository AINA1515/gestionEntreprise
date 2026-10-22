<script setup>
import { useRoute } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
const route = useRoute()
</script>
<template>
  <PageHeader title="Créer une offre d’emploi" :breadcrumb="['Espace RH', 'Offres', 'Nouvelle offre']"
    subtitle="L’offre pourra être créée à partir de la demande validée." />
  <section class="card shadow-sm"><div class="card-body p-4">
    <h2 class="h5">Formulaire de publication</h2>
    <p class="text-body-secondary">Le formulaire de création d’offre sera disponible dans le bloc P14.</p>
    <p v-if="route.query.demandeId" class="mb-0">Demande associée : <strong>#{{ route.query.demandeId }}</strong></p>
  </div></section>
</template>
