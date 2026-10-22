import { computed, ref } from 'vue'

/** Recherche + filtre par département sur une liste d'offres (ref ou computed). */
export function useOffresFilter(offres) {
  const recherche = ref('')
  const departement = ref('')
  const typeContrat = ref('')

  const departements = computed(() =>
    [...new Set(offres.value.map((o) => o.departement).filter(Boolean))].sort(),
  )

  const offresFiltrees = computed(() => {
    const q = recherche.value.trim().toLowerCase()
    return offres.value.filter(
      (o) =>
        (!departement.value || o.departement === departement.value) &&
        (!typeContrat.value || o.typeContrat === typeContrat.value) &&
        (!q || `${o.titre ?? ''} ${o.description ?? ''}`.toLowerCase().includes(q)),
    )
  })

  return { recherche, departement, typeContrat, departements, offresFiltrees }
}
