import { ref } from 'vue'
import { defineStore } from 'pinia'
import { offreService } from '@/services/offreService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useOffresStore = defineStore('offres', () => {
  const offres = ref([])
  const { loading, error, run } = useAsyncAction()

  const charger = () =>
    run(async () => {
      offres.value = await offreService.lister()
    })

  return { offres, loading, error, charger }
})
