import { ref } from 'vue'
import { defineStore } from 'pinia'
import { employeService } from '@/services/employeService'
import { useToast } from '@/composables/useToast'

export const useEmployesStore = defineStore('employes', () => {
  const page = ref(null)
  const employeSelectionne = ref(null)
  const loading = ref(false)
  const toast = useToast()
  async function charger(params = {}) {
    loading.value = true
    try { page.value = await employeService.lister(params); return page.value }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
    finally { loading.value = false }
  }
  async function obtenir(id) {
    loading.value = true
    try { employeSelectionne.value = await employeService.obtenir(id); return employeSelectionne.value }
    catch (error) { toast.error(error.message); error.toastShown = true; throw error }
    finally { loading.value = false }
  }
  return { page, employeSelectionne, loading, charger, obtenir }
})
