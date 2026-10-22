export function useFormErrors() {
  function fromApiError(error) {
    return Object.fromEntries((error?.details || [])
      .filter((detail) => detail?.field)
      .map((detail) => [detail.field, detail.message || error.message || 'Valeur invalide.']))
  }

  return { fromApiError }
}
