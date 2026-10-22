import { useAuthStore } from '@/stores/auth'

function updateVisibility(element, binding) {
  const allowed = useAuthStore().hasPermission(binding.value)
  element.hidden = !allowed
  if (allowed) element.removeAttribute('aria-hidden')
  else element.setAttribute('aria-hidden', 'true')
}

export const canDirective = {
  mounted: updateVisibility,
  updated: updateVisibility,
}

export default canDirective
