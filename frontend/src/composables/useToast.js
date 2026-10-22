import { readonly, ref } from 'vue'

const toasts = ref([])
let nextId = 1

function show(message, variant = 'info', duration = 4500) {
  const id = nextId++
  toasts.value.push({ id, message, variant })
  if (duration > 0) window.setTimeout(() => dismiss(id), duration)
  return id
}

function dismiss(id) { toasts.value = toasts.value.filter((toast) => toast.id !== id) }

export function useToast() {
  return {
    toasts: readonly(toasts),
    show,
    success: (message, duration) => show(message, 'success', duration),
    error: (message, duration) => show(message, 'danger', duration),
    info: (message, duration) => show(message, 'info', duration),
    dismiss,
  }
}
