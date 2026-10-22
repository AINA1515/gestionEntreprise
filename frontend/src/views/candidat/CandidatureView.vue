<script setup>
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOffresStore } from '@/stores/offres'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const offres = useOffresStore()
const { loading, error, run } = useAsyncAction()

const form = reactive({
  nom: '', prenom: '', email: '', telephone: '',
  offreId: route.query.offreId ?? '',
  lettreMotivation: '',
})

onMounted(() => { if (!offres.offres.length) offres.charger() })

async function envoyer() {
  const result = await run(() => candidatureService.deposer({ ...form }))
  if (!error.value && result !== undefined) router.push({ name: 'candidat-offres' })
}
</script>

<template>
  <div class="col-lg-8 mx-auto">
    <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'candidat-offres' }">
      <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour aux offres d'emploi
    </RouterLink>

    <h1 class="h2">Déposer ma candidature</h1>
    <p class="text-body-secondary mb-4">Complétez les informations nécessaires pour postuler.</p>

    <AlertMessage :message="error" />
    <AlertMessage :message="offres.error" />

    <form @submit.prevent="envoyer">
      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">1. Informations du candidat</div>
        <div class="card-body">
          <FormField id="nom" label="Nom *"><input id="nom" v-model="form.nom" class="form-control" required /></FormField>
          <FormField id="prenom" label="Prénom *"><input id="prenom" v-model="form.prenom" class="form-control" required /></FormField>
          <FormField id="email" label="Adresse e-mail *"><input id="email" v-model="form.email" type="email" class="form-control" required /></FormField>
          <FormField id="telephone" label="Téléphone *"><input id="telephone" v-model="form.telephone" type="tel" class="form-control" required /></FormField>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">2. Offre choisie</div>
        <div class="card-body">
          <FormField id="offre" label="Poste souhaité *">
            <select id="offre" v-model="form.offreId" class="form-select" required>
              <option value="" disabled>-- Choisir une offre --</option>
              <option v-for="o in offres.offres" :key="o.id" :value="String(o.id)">{{ o.titre }}</option>
            </select>
          </FormField>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">3. Mon CV</div>
        <div class="card-body">
          <p class="mb-0">
            Votre CV est géré depuis votre espace candidat :
            <RouterLink :to="{ name: 'candidat-cv' }">créer ou importer mon CV</RouterLink>.
          </p>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">4. Message de motivation</div>
        <div class="card-body">
          <FormField id="motivation" label="Pourquoi souhaitez-vous rejoindre notre entreprise ?">
            <textarea id="motivation" v-model="form.lettreMotivation" rows="5" class="form-control"
                      placeholder="Présentez brièvement votre motivation..."></textarea>
          </FormField>
        </div>
      </section>

      <button class="btn btn-primary mb-4" type="submit" :disabled="loading">
        {{ loading ? 'Envoi…' : 'Envoyer ma candidature' }}
      </button>
    </form>
  </div>
</template>
