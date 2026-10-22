import { ref } from 'vue'
import { defineStore } from 'pinia'
import { demandeService } from '@/services/demandeService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useDemandesStore = defineStore('demandes', () => {
  const demandes = ref([])
  const nombreDemandes = ref(null)
  const recentes = ref([])
  const { loading, error, run } = useAsyncAction()

  const chargerDashboard = () =>
    run(async () => {
      const data = await demandeService.dashboard()
      nombreDemandes.value = data.nombreDemandes
      recentes.value = data.recentes
    })

  const charger = () =>
    run(async () => {
      demandes.value = await demandeService.lister()
    })

  /** Renvoie true si la demande est créée. */
  const creer = async (demande) => {
    const created = await run(() => demandeService.creer(demande), { successMessage: 'La demande a été créée.' })
    return !!created
  }

  return { demandes, nombreDemandes, recentes, loading, error, chargerDashboard, charger, creer }
})
