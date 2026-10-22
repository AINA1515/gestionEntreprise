import { ref } from 'vue'
import { defineStore } from 'pinia'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useCandidaturesStore = defineStore('candidatures', () => {
  const candidatures = ref([])
  const stats = ref({ recues: null, entretiensAOrganiser: null, retenues: null })
  const { loading, error, run } = useAsyncAction()

  const charger = () =>
    run(async () => {
      const data = await candidatureService.suivi()
      stats.value = data.stats
      candidatures.value = data.candidatures
    })

  const trouver = (id) => candidatures.value.find((c) => String(c.id) === String(id))

  return { candidatures, stats, loading, error, charger, trouver }
})
