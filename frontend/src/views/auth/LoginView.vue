<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAsyncAction } from '@/composables/useAsyncAction'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const { loading, error, run } = useAsyncAction()

const form = reactive({ identifiant: '', motDePasse: '' })

const avantages = [
  { icon: 'person-lines-fill', titre: 'Gestion des employés', texte: 'Les informations du personnel' },
  { icon: 'calendar-check', titre: 'Suivi des congés', texte: 'Demandes et validations' },
  { icon: 'clipboard-data', titre: 'Gestion des absences', texte: 'Un suivi clair au quotidien' },
  { icon: 'graph-up-arrow', titre: 'Suivi des performances', texte: 'Évaluations et objectifs' },
]

async function seConnecter() {
  await run(() => auth.login(form.identifiant, form.motDePasse))
  if (!error.value) {
    const redirect = typeof route.query.redirect === 'string'
      && route.query.redirect.startsWith('/')
      && !route.query.redirect.startsWith('//')
      ? route.query.redirect
      : auth.homeRoute
    router.push(redirect)
  }
}
</script>

<template>
  <div class="row g-0 min-vh-100">
    <section class="col-lg-5 bg-primary bg-gradient text-white d-flex flex-column justify-content-center p-4 p-lg-5">
      <div class="d-flex align-items-center gap-3 mb-5">
        <i class="bi bi-people-fill display-5" aria-hidden="true"></i>
        <span class="fs-3 fw-semibold">ConceptionLogiciel<span class="text-info">RH</span></span>
      </div>

      <h2 class="display-6 mb-4">Une gestion humaine,<br />une entreprise plus forte</h2>

      <ul class="list-unstyled d-grid gap-3 mb-0">
        <li v-for="a in avantages" :key="a.titre" class="d-flex align-items-center gap-3">
          <span class="border border-light-subtle rounded-3 px-2 py-1 fs-4">
            <i :class="`bi bi-${a.icon}`" aria-hidden="true"></i>
          </span>
          <div>
            <strong class="d-block">{{ a.titre }}</strong>
            <small class="text-white-50">{{ a.texte }}</small>
          </div>
        </li>
      </ul>

      <p class="mt-5 mb-0 small text-white-50">Ensemble pour demain.</p>
    </section>

    <main class="col-lg-7 d-flex flex-column justify-content-center align-items-center p-4">
      <div class="row w-100 justify-content-center">
        <div class="col-12 col-md-9 col-xl-7">
          <section class="card shadow-sm">
            <div class="card-body p-4 p-md-5">
              <h1 class="h2 text-center mb-3">Bienvenue !</h1>
              <p class="text-center text-body-secondary mb-4">
                Connectez-vous à votre compte<br />pour accéder à votre espace RH.
              </p>

              <AlertMessage :message="error" />
              <AlertMessage v-if="route.query.message === 'session-expiree'" variant="warning" message="Session expirée. Veuillez vous reconnecter." />

              <form @submit.prevent="seConnecter">
                <div class="mb-3">
                  <label for="identifiant" class="form-label fw-semibold">Identifiant ou email</label>
                  <div class="input-group input-group-lg">
                    <span class="input-group-text"><i class="bi bi-person" aria-hidden="true"></i></span>
                    <input id="identifiant" v-model="form.identifiant" type="text" class="form-control"
                           placeholder="Saisissez votre identifiant" autocomplete="username" required />
                  </div>
                </div>

                <div class="mb-4">
                  <label for="motDePasse" class="form-label fw-semibold">Mot de passe</label>
                  <div class="input-group input-group-lg">
                    <span class="input-group-text"><i class="bi bi-lock" aria-hidden="true"></i></span>
                    <input id="motDePasse" v-model="form.motDePasse" type="password" class="form-control"
                           placeholder="Saisissez votre mot de passe" autocomplete="current-password" required va />
                  </div>
                </div>

                <button class="btn btn-primary btn-lg w-100" type="submit" :disabled="loading">
                  <i class="bi bi-box-arrow-in-right me-2" aria-hidden="true"></i>
                  {{ loading ? 'Connexion…' : 'Se connecter' }}
                </button>
              </form>

              <p class="text-center text-body-secondary small mt-4 mb-0">
                Besoin d’aide ?<br />
                <span class="text-primary fw-semibold">Contactez votre administrateur.</span>
                <br /><RouterLink :to="{ name: 'inscription' }">Créer un compte candidat</RouterLink>
                <br /><RouterLink :to="{ name: 'offres-publiques' }">Voir les offres d’emploi disponibles</RouterLink>
              </p>
            </div>
          </section>
        </div>
      </div>

      <p class="text-center text-body-secondary small mt-4 mb-0">
        ConceptionLogicielRH · Gestion des ressources humaines
      </p>
    </main>
  </div>
</template>
