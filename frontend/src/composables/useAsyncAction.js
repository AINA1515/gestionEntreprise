import { ref } from 'vue'

/**
 * Enveloppe une action asynchrone et expose loading / error.
 * const { loading, error, run } = useAsyncAction()
 * await run(() => service.appel())   // renvoie le résultat, ou undefined en cas d'erreur
 */
export function useAsyncAction() {
  const loading = ref(false)
  const error = ref(null)

  async function run(action) {
    loading.value = true
    error.value = null
    try {
      return await action()
    } catch (e) {
      error.value = e.message || 'Une erreur est survenue.'
      return undefined
    } finally {
      loading.value = false
    }
  }

  return { loading, error, run }
}
