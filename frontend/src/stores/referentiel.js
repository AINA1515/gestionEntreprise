import { ref } from 'vue'
import { defineStore } from 'pinia'
import { referentielService } from '@/services/referentielService'

export const useReferentielStore = defineStore('referentiel', () => {
  const departements = ref([])
  const postes = ref([])

  async function chargerDepartements() {
    if (!departements.value.length) departements.value = await referentielService.departements()
  }

  async function chargerPostes(departementId) {
    postes.value = await referentielService.postes(departementId)
  }

  return { departements, postes, chargerDepartements, chargerPostes }
})
