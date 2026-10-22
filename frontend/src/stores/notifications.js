import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationService } from '@/services/notificationService'
import { useToast } from '@/composables/useToast'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const page = ref({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 })
  const loading = ref(false)
  const toast = useToast()

  async function actualiser({ limit = 10 } = {}) {
    const [response, count] = await Promise.all([
      notificationService.lister({ page: 0, size: limit }),
      notificationService.compterNonLues(),
    ])
    notifications.value = response.content
    unreadCount.value = count
    return response
  }
  async function chargerPage(pageNumber = 0, size = 20) {
    loading.value = true
    try {
      page.value = await notificationService.lister({ page: pageNumber, size })
      return page.value
    } finally { loading.value = false }
  }
  async function marquerLue(id) {
    await notificationService.marquerLue(id)
    const item = notifications.value.find((n) => n.id === id)
    if (item && !item.lue) { item.lue = true; unreadCount.value = Math.max(0, unreadCount.value - 1) }
    page.value.content = page.value.content.map((n) => n.id === id ? { ...n, lue: true } : n)
  }
  async function lireTout() {
    await notificationService.lireTout()
    notifications.value = notifications.value.map((n) => ({ ...n, lue: true }))
    page.value.content = page.value.content.map((n) => ({ ...n, lue: true }))
    unreadCount.value = 0
    toast.success('Toutes les notifications sont marquées comme lues.')
  }
  return { notifications, unreadCount, page, loading, actualiser, chargerPage, marquerLue, lireTout }
})
