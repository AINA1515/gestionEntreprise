import http from './http'

export const notificationService = {
  lister: (params = {}) => http.get('/notifications', { params }).then((response) => response.data),
  compterNonLues: () => http.get('/notifications/non-lues/count').then((response) => response.data.count),
  marquerLue: (id) => http.patch(`/notifications/${id}/lue`),
  lireTout: () => http.patch('/notifications/lire-tout').then((response) => response.data.count),
}
