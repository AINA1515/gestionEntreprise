<script setup>
</script>

<template>
  <div class="min-vh-100 bg-body-tertiary">
    <header class="navbar navbar-expand bg-white border-bottom shadow-sm">
      <div class="container">
        <RouterLink class="navbar-brand fw-semibold text-primary" :to="{ name: 'offres-publiques' }">
          <i class="bi bi-people-fill me-2" aria-hidden="true"></i>ConceptionLogicielRH
        </RouterLink>
        <nav class="d-flex gap-2">
          <RouterLink class="btn btn-outline-primary btn-sm" :to="{ name: 'login' }">Se connecter</RouterLink>
          <RouterLink class="btn btn-primary btn-sm" :to="{ name: 'inscription' }">S’inscrire</RouterLink>
        </nav>
      </div>
    </header>
    <main class="container py-4 py-lg-5"><RouterView /></main>
    <footer class="container py-4 border-top text-body-secondary small">ConceptionLogicielRH · Recrutement</footer>
  </div>
</template>
