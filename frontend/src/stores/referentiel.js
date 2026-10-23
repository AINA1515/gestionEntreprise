import { ref } from 'vue'
import { defineStore } from 'pinia'
import { referentielService } from '@/services/referentielService'
import { useToast } from '@/composables/useToast'

export const useReferentielStore = defineStore('referentiel', () => {
  const departements = ref([])
  const postes = ref([])
  const niveauxFormation = ref([])
  const departementsPage = ref(null)
  const postesPage = ref(null)
  const departementSelectionne = ref(null)
  const toast = useToast()

  async function chargerDepartements() {
    try { if (!departements.value.length) departements.value = await referentielService.departements() }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
  }

  async function chargerNiveauxFormation() {
    try { if (!niveauxFormation.value.length) niveauxFormation.value = await referentielService.niveauxFormation() }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
  }

  async function chargerPostes(departementId) {
    try { postes.value = await referentielService.postes(departementId) }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
  }
  async function chargerDepartement(id) {
    try { departementSelectionne.value = await referentielService.departement(id); return departementSelectionne.value }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
  }

  async function chargerDepartementsPage(params = {}) {
    try { departementsPage.value = await referentielService.listerDepartements(params); departements.value = departementsPage.value.content }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
  }
  async function creerDepartement(data) { try { const item = await referentielService.creerDepartement(data); toast.success('Département créé.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function modifierDepartement(id, data) { try { const item = await referentielService.modifierDepartement(id, data); toast.success('Département modifié.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function changerResponsable(id, data) { try { const item = await referentielService.changerResponsable(id, data); toast.success('Responsable du département modifié.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function desactiverDepartement(id) { try { await referentielService.desactiverDepartement(id); toast.success('Département désactivé.') } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function chargerPostesPage(params = {}) { try { postesPage.value = await referentielService.listerPostes(params) } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function creerPoste(data) { try { const item = await referentielService.creerPoste(data); toast.success('Poste créé et ouvert en statut vacant.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function modifierPoste(id, data) { try { const item = await referentielService.modifierPoste(id, data); toast.success('Poste modifié.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }
  async function fermerPoste(id) { try { const item = await referentielService.fermerPoste(id); toast.success('Poste fermé.'); return item } catch (error) { toast.error(error.message); error.toastShown = true; throw error } }

  return { departements, postes, niveauxFormation, departementsPage, postesPage, departementSelectionne, chargerDepartement, chargerDepartements, chargerPostes, chargerNiveauxFormation,
    chargerDepartementsPage, creerDepartement, modifierDepartement, changerResponsable, desactiverDepartement,
    chargerPostesPage, creerPoste, modifierPoste, fermerPoste }
})
