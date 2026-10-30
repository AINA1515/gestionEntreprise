import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

// Même priorité que la page d'accueil après connexion (stores/auth.js) : le menu ne dépend
// que du profil de l'utilisateur, jamais de la page affichée.
const PRIORITE = ['ADMIN', 'MANAGER', 'GESTIONNAIRE_RH', 'DIRECTION', 'SALARIE', 'CANDIDAT', 'IT', 'MOYENS_GENERAUX']

const ESPACES = {
  ADMIN: 'Administration', MANAGER: 'Espace Manager', GESTIONNAIRE_RH: 'Espace Ressources humaines', DIRECTION: 'Direction',
  SALARIE: 'Espace salarié', CANDIDAT: 'Espace candidat', IT: 'Espace informatique', MOYENS_GENERAUX: 'Moyens généraux',
}

const MENUS = {
  ADMIN: [
    { to: '/admin/utilisateurs', label: 'Utilisateurs', icon: 'people', permission: 'UTILISATEUR_ADMIN' },
  ],
  MANAGER: [
    { to: '/manager/dashboard', label: 'Tableau de bord', icon: 'grid' },
    { to: '/manager/demandes/nouvelle', label: 'Nouvelle demande', icon: 'plus-lg', permission: 'DEMANDE_CREER' },
    { to: '/manager/demandes', label: 'Mes demandes', icon: 'card-list', permission: 'DEMANDE_CONSULTER' },
    { to: '/manager/entretiens', label: 'Mes entretiens', icon: 'calendar-event', permission: 'EVALUATION_SAISIR' },
    { to: '/manager/onboarding', label: 'Tâches d’intégration', icon: 'person-check', permission: 'DEMANDE_CREER' },
  ],
  GESTIONNAIRE_RH: [
    { to: '/rh/accueil', label: 'Accueil RH', icon: 'grid' },
    { to: '/rh/demandes-a-traiter', label: 'Demandes à vérifier', icon: 'clipboard-check', permission: 'DEMANDE_VERIFIER_RH' },
    { to: '/rh/demandes-direction', label: 'Décisions de la Direction', icon: 'building-check', permission: 'DEMANDE_VERIFIER_RH' },
    { to: '/rh/candidatures', label: 'Candidatures', icon: 'people', permission: 'CANDIDATURE_CONSULTER' },
    { to: '/rh/confirmation', label: 'Propositions', icon: 'envelope-check', permission: 'PROPOSITION_EMETTRE' },
    { to: '/rh/offres', label: 'Offres d’emploi', icon: 'briefcase', permission: 'OFFRE_PUBLIER' },
    { to: '/rh/departements', label: 'Départements', icon: 'diagram-3', permission: 'CONTRAT_GERER' },
    { to: '/rh/postes', label: 'Postes', icon: 'briefcase', permission: 'CONTRAT_GERER' },
    { to: '/rh/affectations', label: 'Affectations', icon: 'arrow-left-right', permission: 'CONTRAT_GERER' },
    { to: '/rh/employes', label: 'Employés', icon: 'people-fill', permission: 'CONTRAT_GERER' },
  ],
  DIRECTION: [
    { to: '/direction/dashboard', label: 'Tableau de bord', icon: 'grid', permission: 'REPORTING_CONSULTER' },
    { to: '/direction/arbitrage', label: 'Arbitrage des demandes', icon: 'bank', permission: 'DEMANDE_APPROUVER_DIR' },
  ],
  SALARIE: [
    { to: '/salarie/accueil', label: 'Accueil', icon: 'house' },
    { to: '/salarie/contrats', label: 'Mes contrats', icon: 'file-earmark-text', permission: 'DOCUMENT_CONSULTER_PERSO' },
  ],
  CANDIDAT: [
    { to: '/candidat/cv', label: 'Mon CV', icon: 'file-person', permission: 'DOCUMENT_CONSULTER_PERSO' },
    { to: '/candidat/candidature', label: 'Mes candidatures', icon: 'briefcase', permission: 'CANDIDATURE_POSTULER' },
    { to: '/candidat/propositions', label: 'Mes propositions', icon: 'envelope-check', permission: 'PROPOSITION_REPONDRE' },
  ],
  IT: [],
  MOYENS_GENERAUX: [],
}

// Liens présents pour tout le monde, au même endroit quelle que soit la page.
const COMMUNS = [
  { to: '/taches', label: 'Mes tâches', icon: 'check2-square', permission: 'TACHE_TRAITER' },
  { to: '/offres', label: 'Offres d’emploi', icon: 'search' },
  { to: '/notifications', label: 'Notifications', icon: 'bell' },
]

export function useRoleMenu() {
  const auth = useAuthStore()
  const principal = computed(() => PRIORITE.find((role) => auth.hasRole(role)) ?? null)
  const espace = computed(() => ESPACES[principal.value] ?? 'Espace personnel')
  const items = computed(() => {
    const propres = MENUS[principal.value] ?? []
    // Un manager, un RH… est aussi salarié : il garde l'accès à ses propres contrats.
    const contrats = principal.value !== 'SALARIE' && auth.hasRole('SALARIE') && auth.hasPermission('DOCUMENT_CONSULTER_PERSO')
      ? [{ to: '/salarie/contrats', label: 'Mes contrats', icon: 'file-earmark-text' }] : []
    return [...propres, ...contrats, ...COMMUNS].filter((item) => !item.permission || auth.hasPermission(item.permission))
  })
  return { items, espace, principal }
}
