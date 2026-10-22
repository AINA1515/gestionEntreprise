import { defineStore } from 'pinia'
import { ref } from 'vue'
import { documentService } from '@/services/documentService'
import { useToast } from '@/composables/useToast'

export const useDocumentsStore = defineStore('documents', () => {
  const items = ref([])
  const loading = ref(false)
  const progress = ref(0)
  const toast = useToast()

  async function lister(entite, id) {
    loading.value = true
    try { items.value = await documentService.lister(entite, id); return items.value }
    finally { loading.value = false }
  }
  async function importerCv(fichier) {
    progress.value = 0
    try {
      const item = await documentService.importerCv(fichier, (event) => { if (event.total) progress.value = Math.round(event.loaded * 100 / event.total) })
      toast.success('CV importé avec succès.')
      return item
    } catch (error) { toast.error(error.message || 'L’import du CV a échoué.'); throw error }
  }
  async function telecharger(id) {
    try {
      const response = await documentService.telecharger(id)
      const url = URL.createObjectURL(response.data)
      const link = document.createElement('a')
      link.href = url
      link.download = decodeURIComponent(response.headers['content-disposition']?.split('filename*=UTF-8\'\'')[1] || 'document')
      document.body.appendChild(link); link.click(); link.remove(); URL.revokeObjectURL(url)
    } catch (error) { toast.error(error.message || 'Le téléchargement a échoué.'); throw error }
  }
  async function archiver(id) {
    await documentService.archiver(id)
    items.value = items.value.filter((document) => document.id !== id)
    toast.success('Document archivé.')
  }
  return { items, loading, progress, lister, importerCv, telecharger, archiver }
})
