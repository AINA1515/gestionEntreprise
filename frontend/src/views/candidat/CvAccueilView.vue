<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
const suite = route.query.offreId ? { query: { offreId: String(route.query.offreId) } } : {}
</script>

<template>
  <nav aria-label="breadcrumb">
    <ol class="breadcrumb">
      <li class="breadcrumb-item">Accueil</li>
      <li class="breadcrumb-item active" aria-current="page">Mon CV</li>
    </ol>
  </nav>

  <h1 class="h2">Préparez votre candidature</h1>
  <p class="text-body-secondary mb-4">Choisissez comment vous souhaitez préparer votre CV.</p>

  <div class="row g-4">
    <div class="col-md-6">
      <section class="card h-100 shadow-sm">
        <div class="card-body d-flex flex-column">
          <i class="bi bi-pencil-square display-5 text-primary mb-3" aria-hidden="true"></i>
          <h2 class="h5 card-title">Créer mon CV</h2>
          <p class="card-text text-body-secondary">
            Complétez vos informations personnelles, vos formations, vos expériences et vos compétences.
          </p>
          <RouterLink class="btn btn-primary mt-auto align-self-start" :to="{ name: 'candidat-cv-creer', ...suite }">
            Compléter mon CV
          </RouterLink>
        </div>
      </section>
    </div>

    <div class="col-md-6">
      <section class="card h-100 shadow-sm">
        <div class="card-body d-flex flex-column">
          <i class="bi bi-file-earmark-arrow-up display-5 text-primary mb-3" aria-hidden="true"></i>
          <h2 class="h5 card-title">Importer un CV existant</h2>
          <p class="card-text text-body-secondary">
            Sélectionnez un fichier CV sur votre ordinateur au format PDF ou DOCX.
          </p>
          <RouterLink class="btn btn-dark mt-auto align-self-start" :to="{ name: 'candidat-cv-importer', ...suite }">
            Choisir mon fichier
          </RouterLink>
        </div>
      </section>
    </div>

    <div class="col-12">
      <section class="card shadow-sm">
        <div class="card-body">
          <i class="bi bi-briefcase display-5 text-primary mb-3 d-block" aria-hidden="true"></i>
          <h2 class="h5 card-title">Offres d'emploi</h2>
          <p class="card-text text-body-secondary">
            Découvrez les postes disponibles et trouvez une opportunité qui vous correspond.
          </p>
          <RouterLink class="btn btn-primary" :to="{ name: 'candidat-offres' }">Consulter les offres</RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>
