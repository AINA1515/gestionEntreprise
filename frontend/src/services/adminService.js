import http from './http'

export const adminService = {
  utilisateurs: () => http.get('/admin/utilisateurs').then((r) => r.data),
  creer: (payload) => http.post('/admin/utilisateurs', payload).then((r) => r.data),
  modifier: (id, payload) => http.patch(`/admin/utilisateurs/${id}`, payload).then((r) => r.data),
  definirRoles: (id, roles) => http.put(`/admin/utilisateurs/${id}/roles`, { roles }).then((r) => r.data),
  roles: () => http.get('/admin/roles').then((r) => r.data),
  permissions: () => http.get('/admin/permissions').then((r) => r.data),
  permissionsRole: (code) => http.get(`/admin/roles/${code}/permissions`).then((r) => r.data),
  definirPermissions: (code, codes) => http.put(`/admin/roles/${code}/permissions`, codes).then((r) => r.data),
}
