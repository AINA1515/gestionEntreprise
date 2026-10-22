<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncAction } from '@/composables/useAsyncAction'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import FileUploader from '@/components/ui/FileUploader.vue'
import { useDocumentsStore } from '@/stores/documents'

const router = useRouter()
const { loading, error, run } = useAsyncAction()
const fichier = ref(null)
const documents = useDocumentsStore()

async function envoyer() {
  const result = await run(() => documents.importerCv(fichier.value))
  if (!error.value && result !== undefined) router.push({ name: 'candidat-cv' })
}
</script>

<template>
  <div class="col-lg-8 mx-auto">
    <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'candidat-cv' }">
      <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour à mon espace candidat
    </RouterLink>

    <h1 class="h2">Importer un CV existant</h1>
    <p class="text-body-secondary mb-4">Sélectionnez un fichier au format PDF ou DOCX.</p>

    <AlertMessage :message="error" />

    <form class="card shadow-sm" @submit.prevent="envoyer">
      <div class="card-body p-4">
        <label class="form-label">Mon CV (PDF ou DOCX)</label>
        <FileUploader v-model="fichier" :accept="['.pdf', '.docx']" :progress="documents.progress" :disabled="loading" class="mb-4" />
        <button class="btn btn-primary" type="submit" :disabled="loading || !fichier">
          {{ loading ? 'Envoi…' : 'Importer mon CV' }}
        </button>
      </div>
    </form>
  </div>
</template>
