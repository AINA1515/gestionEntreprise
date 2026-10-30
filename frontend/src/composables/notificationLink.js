// Page vers laquelle mène une notification, selon son type et le profil de la personne qui la lit.
// Retourne null quand aucune page ne correspond : la notification reste alors dans la liste.
export function lienNotification(n, auth) {
  const id = n.entiteId
  const manager = auth.hasRole('MANAGER')
  const rh = auth.hasRole('GESTIONNAIRE_RH')
  switch (n.codeModele) {
    case 'DEMANDE_SOUMISE':
      return rh && id ? { name: 'rh-demande-revue', params: { id } } : null
    case 'DEMANDE_TRANSMISE_DIRECTION':
      return auth.hasRole('DIRECTION') && id ? { name: 'direction-demande-arbitrage', params: { id } } : null
    case 'DEMANDE_VALIDEE':
    case 'DEMANDE_REFUSEE':
      if (manager && id) return { name: 'manager-demande-detail', params: { id } }
      return rh ? { name: 'rh-demandes-direction' } : null
    case 'OFFRE_PUBLIEE':
      return id ? { name: 'offre-detail', params: { id } } : { name: 'offres-publiques' }
    case 'CANDIDATURE_PRESELECTIONNEE':
      return manager ? { name: 'manager-entretiens' } : rh ? { name: 'rh-candidatures' } : null
    case 'ENTRETIEN_PLANIFIE':
      return auth.hasRole('CANDIDAT') ? { name: 'candidat-candidature' } : manager ? { name: 'manager-entretiens' } : null
    case 'CANDIDATURE_REJETEE':
    case 'CANDIDATURE_RECUE':
      return auth.hasRole('CANDIDAT') ? { name: 'candidat-candidature' } : rh ? { name: 'rh-candidatures' } : null
    case 'PROPOSITION_ENVOYEE':
      return { name: 'candidat-propositions' }
    case 'PROPOSITION_ACCEPTEE':
    case 'PROPOSITION_REFUSEE':
      return rh ? { name: 'rh-confirmation' } : null
    case 'EMPLOYE_CREE':
      return rh && id ? { name: 'rh-employe-detail', params: { id } } : rh ? { name: 'rh-employes' } : null
    case 'ONBOARDING_TACHE_ASSIGNEE':
    case 'ONBOARDING_TACHE_EN_RETARD':
      return { name: 'mes-taches' }
    case 'CONTRAT_FIN_PROCHE':
      return rh ? { name: 'rh-employes' } : { name: 'salarie-contrats' }
    default:
      return null
  }
}

/** Texte lisible du corps HTML d'une notification, sans son titre (déjà affiché comme sujet). */
export function texteNotification(n) {
  const html = (n.corps || '').replace(/<h1[^>]*>[\s\S]*?<\/h1>/i, '')
  return html.replace(/<[^>]*>/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/\s+/g, ' ').trim()
}
