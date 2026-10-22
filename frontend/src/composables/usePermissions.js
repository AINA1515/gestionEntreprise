import { useAuthStore } from '@/stores/auth'

export function usePermissions() {
  const auth = useAuthStore()
  return {
    can: (permission) => auth.hasPermission(permission),
    hasRole: (role) => auth.hasRole(role),
    canAny: (permissions) => auth.hasAnyPermission(permissions),
  }
}
