<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAsyncAction } from '@/composables/useAsyncAction'
import PageHeader from '@/components/ui/PageHeader.vue'
import AppLayout from '@/components/layout/AppLayout.vue'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ ancien: '', nouveau: '', confirmation: '' })
const { loading, error, run } = useAsyncAction()

async function submit() {
  if (form.nouveau !== form.confirmation) return
  await run(() => auth.changerMotDePasse(form.ancien, form.nouveau))
  if (!error.value) {
    router.push(auth.homeRoute)
  }
}
</script>

<template>
  <AppLayout espace="Mon profil" :items="[]">
    <div class="container py-3" style="max-width: 44rem">
      <PageHeader title="Changer le mot de passe" />
      <div v-if="error" class="alert alert-danger" role="alert">{{ error }}</div>
      <form class="card card-body" @submit.prevent="submit">
        <label class="form-label" for="ancien">Mot de passe actuel</label>
        <input id="ancien" v-model="form.ancien" class="form-control mb-3" type="password" autocomplete="current-password" required />
        <label class="form-label" for="nouveau">Nouveau mot de passe</label>
        <input id="nouveau" v-model="form.nouveau" class="form-control mb-3" type="password" minlength="12" autocomplete="new-password" required />
        <label class="form-label" for="confirmation">Confirmer le nouveau mot de passe</label>
        <input id="confirmation" v-model="form.confirmation" class="form-control mb-3" type="password" minlength="12" autocomplete="new-password" required />
        <p v-if="form.confirmation && form.nouveau !== form.confirmation" class="text-danger small">Les mots de passe ne correspondent pas.</p>
        <button class="btn btn-primary align-self-start" type="submit" :disabled="loading || form.nouveau !== form.confirmation">{{ loading ? 'Enregistrement…' : 'Enregistrer' }}</button>
      </form>
    </div>
  </AppLayout>
</template>
