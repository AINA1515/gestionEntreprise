<script setup>
import AppLayout from './AppLayout.vue'

const items = [
  { to: '/manager/dashboard', label: 'Tableau de bord', icon: 'grid', roles: ['MANAGER'] },
  { to: '/manager/demandes/nouvelle', label: 'Nouvelle demande', icon: 'plus-lg', roles: ['MANAGER'], permission: 'DEMANDE_CREER' },
  { to: '/manager/demandes', label: 'Mes demandes', icon: 'card-list', roles: ['MANAGER'], permission: 'DEMANDE_CONSULTER' },
]
</script>

<template>
  <AppLayout espace="Espace Manager" :items="items">
    <RouterView />
  </AppLayout>
</template>
