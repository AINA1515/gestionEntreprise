<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/services/authService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const router = useRouter()
const { loading, error, run } = useAsyncAction()
const success = ref('')
const form = reactive({ nom: '', prenom: '', email: '', telephone: '', motDePasse: '', consentementRgpd: false })

async function inscrire() {
  success.value = ''
  const result = await run(() => authService.inscriptionCandidat({ ...form }))
  if (result !== undefined) {
    success.value = 'Votre compte a été créé. Vous pouvez maintenant vous connecter.'
    window.setTimeout(() => router.push({ name: 'login' }), 900)
  }
}
</script>

<template>
  <main class="container py-5" style="max-width: 680px">
    <section class="card shadow-sm">
      <div class="card-body p-4 p-md-5">
        <h1 class="h2 mb-2">Créer un compte candidat</h1>
        <p class="text-body-secondary mb-4">Renseignez vos informations pour accéder à votre espace candidat.</p>
        <AlertMessage :message="error" />
        <AlertMessage v-if="success" variant="success" :message="success" />
        <form class="row g-3" @submit.prevent="inscrire">
          <div class="col-md-6"><label for="prenom" class="form-label">Prénom</label><input id="prenom" v-model.trim="form.prenom" class="form-control" maxlength="100" required /></div>
          <div class="col-md-6"><label for="nom" class="form-label">Nom</label><input id="nom" v-model.trim="form.nom" class="form-control" maxlength="100" required /></div>
          <div class="col-12"><label for="email" class="form-label">Adresse courriel</label><input id="email" v-model.trim="form.email" type="email" class="form-control" maxlength="150" autocomplete="email" required /></div>
          <div class="col-12"><label for="telephone" class="form-label">Téléphone <span class="text-body-secondary">(facultatif)</span></label><input id="telephone" v-model.trim="form.telephone" type="tel" class="form-control" maxlength="30" autocomplete="tel" /></div>
          <div class="col-12"><label for="motDePasse" class="form-label">Mot de passe</label><input id="motDePasse" v-model="form.motDePasse" type="password" class="form-control" minlength="12" maxlength="72" autocomplete="new-password" required /><div class="form-text">12 à 72 caractères, avec une minuscule, une majuscule, un chiffre et un caractère spécial.</div></div>
          <div class="col-12 form-check ms-2"><input id="consentement" v-model="form.consentementRgpd" type="checkbox" class="form-check-input" required /><label for="consentement" class="form-check-label">J’accepte le traitement de mes données pour gérer ma candidature.</label></div>
          <div class="col-12 d-grid"><button class="btn btn-primary btn-lg" type="submit" :disabled="loading">{{ loading ? 'Création…' : 'Créer mon compte' }}</button></div>
        </form>
        <p class="text-center mt-4 mb-0">Déjà inscrit ? <RouterLink :to="{ name: 'login' }">Se connecter</RouterLink></p>
      </div>
    </section>
  </main>
</template>
