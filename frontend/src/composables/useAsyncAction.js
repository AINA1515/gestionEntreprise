import { ref } from 'vue'
import { useToast } from '@/composables/useToast'

/**
 * Enveloppe une action asynchrone et expose loading / error.
 * const { loading, error, run } = useAsyncAction()
 * await run(() => service.appel())   // renvoie le résultat, ou undefined en cas d'erreur
 */
export function useAsyncAction() {
  const loading = ref(false)
  const error = ref(null)
  const toast = useToast()

  async function run(action, options = {}) {
    loading.value = true
    error.value = null
    try {
      const result = await action()
      if (options.successMessage) toast.success(options.successMessage)
      return result
    } catch (e) {
      error.value = e.message || 'Une erreur est survenue.'
      if (!e.toastShown) toast.error(error.value)
      return undefined
    } finally {
      loading.value = false
    }
  }

  return { loading, error, run }
}
