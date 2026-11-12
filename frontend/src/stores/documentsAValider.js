import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { documentService } from '@/services/documentService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useDocumentsAValiderStore = defineStore('documents-a-valider', () => {
  const documents = ref([])
  const { loading, error, run } = useAsyncAction()
  const nombre = computed(() => documents.value.length)

  async function charger() {
    const resultat = await run(() => documentService.aValider())
    if (resultat) documents.value = resultat
    return documents.value
  }

  return { documents, nombre, loading, error, charger }
})
