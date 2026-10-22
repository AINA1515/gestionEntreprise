import { ref } from 'vue'
import { defineStore } from 'pinia'
import { demandeService } from '@/services/demandeService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useDemandesStore = defineStore('demandes', () => {
  const demandes = ref([])
  const page = ref({ number: 0, size: 20, totalElements: 0, totalPages: 0 })
  const demandeSelectionnee = ref(null)
  const nombreDemandes = ref(null)
  const parStatut = ref({})
  const recentes = ref([])
  const { loading, error, details: errorDetails, run } = useAsyncAction()

  const chargerDashboard = () =>
    run(async () => {
      const data = await demandeService.dashboard()
      nombreDemandes.value = data.nombreDemandes
      parStatut.value = data.parStatut || {}
      recentes.value = data.recentes
    })

  const charger = (params = {}) =>
    run(async () => {
      const response = await demandeService.lister(params)
      demandes.value = response.content
      page.value = { number: response.page, size: response.size, totalElements: response.totalElements, totalPages: response.totalPages }
      return response
    })

  const obtenir = (id) => run(async () => {
    demandeSelectionnee.value = await demandeService.detail(id)
    return demandeSelectionnee.value
  })

  /** Renvoie true si la demande est créée. */
  const creer = async (demande) => {
    const created = await run(() => demandeService.creer(demande), { successMessage: 'La demande a été créée.' })
    return created
  }

  const modifier = async (id, demande) => {
    const updated = await run(() => demandeService.modifier(id, demande), { successMessage: 'La demande a été modifiée.' })
    if (updated) demandeSelectionnee.value = { demande: updated, historiqueValidations: [], offresLiees: [], actionsDisponibles: updated.actionsDisponibles }
    return !!updated
  }

  const soumettre = async (id) => {
    const updated = await run(() => demandeService.soumettre(id), { successMessage: 'La demande a été soumise à la RH.' })
    return !!updated
  }

  const valider = async (id, validation) => run(
    () => demandeService.valider(id, validation),
    { successMessage: 'La décision a été enregistrée.' },
  )

  const supprimer = async (id) => !!(await run(() => demandeService.supprimer(id), { successMessage: 'La demande a été supprimée.' }))

  return { demandes, page, demandeSelectionnee, nombreDemandes, parStatut, recentes, loading, error, errorDetails,
    chargerDashboard, charger, obtenir, creer, modifier, soumettre, valider, supprimer }
})
