<script setup>
import SidebarLayout from './SidebarLayout.vue'

const items = [
  { to: '/manager/dashboard', label: 'Tableau de bord', icon: 'grid' },
  { to: '/manager/demandes/nouvelle', label: 'Nouvelle demande', icon: 'plus-lg' },
  { to: '/manager/demandes', label: 'Mes demandes', icon: 'card-list' },
]
</script>

<template>
  <SidebarLayout espace="Espace Manager" :items="items">
    <RouterView />
  </SidebarLayout>
</template>
