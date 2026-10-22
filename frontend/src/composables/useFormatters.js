const dateFormat = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' })
const moneyFormat = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 })

export function useFormatters() {
  const formatDate = (value) => (value ? dateFormat.format(new Date(value)) : '—')
  const formatMontant = (value) => (value == null ? '—' : moneyFormat.format(value))
  return { formatDate, formatMontant }
}
