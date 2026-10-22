import { ref } from 'vue'
import { defineStore } from 'pinia'
import { offreService } from '@/services/offreService'
import { useAsyncAction } from '@/composables/useAsyncAction'

export const useOffresStore = defineStore('offres', () => {
  const offres = ref([])
  const offreSelectionnee = ref(null)
  const page = ref({ number: 0, size: 12, totalElements: 0, totalPages: 0 })
  const { loading, error, run } = useAsyncAction()

  const charger = (params = {}) =>
    run(async () => {
      const response = await offreService.lister({ page: 0, size: 12, ...params })
      offres.value = response.content ?? []
      page.value = { number: response.page ?? 0, size: response.size ?? 12,
        totalElements: response.totalElements ?? offres.value.length, totalPages: response.totalPages ?? 1 }
      return response
    })

  const obtenir = (id) => run(async () => {
    offreSelectionnee.value = await offreService.detail(id)
    return offreSelectionnee.value
  })
  const creer = (data) => run(() => offreService.creer(data), { successMessage: 'L’offre a été créée en brouillon.' })
  const modifier = (id, data) => run(() => offreService.modifier(id, data), { successMessage: 'L’offre a été modifiée.' })
  const publier = (id) => run(() => offreService.publier(id), { successMessage: 'L’offre est publiée.' })
  const cloturer = (id) => run(() => offreService.cloturer(id), { successMessage: 'L’offre a été clôturée.' })

  return { offres, offreSelectionnee, page, loading, error, charger, obtenir, creer, modifier, publier, cloturer }
})
