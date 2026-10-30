import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ManagerLayout from '@/components/layout/ManagerLayout.vue'
import RhLayout from '@/components/layout/RhLayout.vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import DirectionLayout from '@/components/layout/DirectionLayout.vue'
import PublicLayout from '@/components/layout/PublicLayout.vue'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeRedirectView.vue'), meta: { requiresAuth: true } },
  { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue'), meta: { public: true } },
  { path: '/inscription', name: 'inscription', component: () => import('@/views/auth/RegisterView.vue'), meta: { public: true } },
  { path: '/403', name: 'forbidden', component: () => import('@/views/ForbiddenView.vue'), meta: { public: true } },
  {
    path: '/offres', component: PublicLayout, meta: { public: true, espace: 'Offres d’emploi' },
    children: [
      { path: '', name: 'offres-publiques', component: () => import('@/views/candidat/OffresView.vue'), meta: { public: true } },
      { path: ':id', name: 'offre-detail', component: () => import('@/views/candidat/OffreDetailView.vue'), meta: { public: true } },
    ],
  },
  {
    path: '/manager', component: ManagerLayout, meta: { requiresAuth: true, roles: ['MANAGER'], espace: 'Espace Manager' },
    children: [
      { path: '', redirect: { name: 'manager-dashboard' } },
      { path: 'dashboard', name: 'manager-dashboard', component: () => import('@/views/manager/DashboardView.vue'), meta: { permissions: ['DEMANDE_CONSULTER'] } },
      { path: 'demandes', name: 'manager-demandes', component: () => import('@/views/manager/MesDemandesView.vue'), meta: { permissions: ['DEMANDE_CONSULTER'] } },
      { path: 'demandes/nouvelle', name: 'manager-nouvelle-demande', component: () => import('@/views/manager/NouvelleDemandeView.vue'), meta: { permissions: ['DEMANDE_CREER'] } },
      { path: 'demandes/:id', name: 'manager-demande-detail', component: () => import('@/views/manager/DemandeDetailView.vue'), meta: { permissions: ['DEMANDE_CONSULTER'] } },
      { path: 'demandes/:id/modifier', name: 'manager-demande-modifier', component: () => import('@/views/manager/NouvelleDemandeView.vue'), meta: { permissions: ['DEMANDE_CREER'] } },
      { path: 'entretiens', name: 'manager-entretiens', component: () => import('@/views/manager/MesEntretiensView.vue'), meta: { permissions: ['EVALUATION_SAISIR'] } },
      { path: 'onboarding', name: 'manager-onboarding', component: () => import('@/views/manager/OnboardingView.vue'), meta: { permissions: ['DEMANDE_CREER'] } },
    ],
  },
  {
    path: '/rh', component: RhLayout, meta: { requiresAuth: true, roles: ['GESTIONNAIRE_RH'], espace: 'Espace Ressources humaines' },
    children: [
      { path: '', redirect: { name: 'rh-accueil' } },
      { path: 'accueil', name: 'rh-accueil', component: () => import('@/views/rh/AccueilView.vue') },
      { path: 'demandes-a-traiter', name: 'rh-demandes-a-traiter', component: () => import('@/views/rh/DemandesATraiterView.vue'), meta: { permissions: ['DEMANDE_VERIFIER_RH'] } },
      { path: 'demandes-direction', name: 'rh-demandes-direction', component: () => import('@/views/rh/DemandesDirectionView.vue'), meta: { permissions: ['DEMANDE_VERIFIER_RH'] } },
      { path: 'demandes/:id/revue', name: 'rh-demande-revue', component: () => import('@/views/rh/DemandeRevueView.vue'), meta: { permissions: ['DEMANDE_VERIFIER_RH'] } },
      { path: 'offres', name: 'rh-offres', component: () => import('@/views/rh/OffresView.vue'), meta: { permissions: ['OFFRE_PUBLIER'] } },
      { path: 'offres/nouvelle', name: 'rh-offre-nouvelle', component: () => import('@/views/rh/OffreFormView.vue'), meta: { permissions: ['OFFRE_PUBLIER'] } },
      { path: 'offres/:id/candidatures', name: 'rh-offre-candidatures', component: () => import('@/views/rh/OffreCandidaturesView.vue'), meta: { permissions: ['CANDIDATURE_CONSULTER'] } },
      { path: 'offres/:id/modifier', name: 'rh-offre-modifier', component: () => import('@/views/rh/OffreFormView.vue'), meta: { permissions: ['OFFRE_PUBLIER'] } },
      { path: 'candidatures', name: 'rh-candidatures', component: () => import('@/views/rh/CandidaturesView.vue'), meta: { permissions: ['CANDIDATURE_CONSULTER'] } },
      { path: 'departements', name: 'rh-departements', component: () => import('@/views/rh/DepartementsView.vue'), meta: { permissions: ['CONTRAT_GERER'] } },
      { path: 'postes', name: 'rh-postes', component: () => import('@/views/rh/PostesView.vue'), meta: { permissions: ['CONTRAT_GERER'] } },
      { path: 'employes', name: 'rh-employes', component: () => import('@/views/rh/EmployesView.vue'), meta: { permissions: ['CONTRAT_GERER'] } },
      { path: 'affectations', name: 'rh-affectations', component: () => import('@/views/rh/AffectationsView.vue'), meta: { permissions: ['CONTRAT_GERER'] } },
      { path: 'employes/:id', name: 'rh-employe-detail', component: () => import('@/views/rh/EmployeDetailView.vue'), meta: { permissions: ['CONTRAT_GERER'] } },
    ],
  },
  {
    path: '/rh', component: PageLayout, meta: { requiresAuth: true, roles: ['GESTIONNAIRE_RH'], espace: 'Espace Ressources humaines' },
    children: [
      { path: 'decision', name: 'rh-decision', component: () => import('@/views/rh/DecisionView.vue'), meta: { roles: ['MANAGER'], permissions: ['EVALUATION_SAISIR'] } },
      { path: 'confirmation', name: 'rh-confirmation', component: () => import('@/views/rh/ConfirmationView.vue'), meta: { permissions: ['PROPOSITION_EMETTRE'] } },
    ],
  },
  {
    path: '/candidat', component: PageLayout, meta: { requiresAuth: true, roles: ['CANDIDAT'], espace: 'Espace candidat' },
    children: [
      { path: '', redirect: { name: 'candidat-cv' } },
      { path: 'cv', name: 'candidat-cv', component: () => import('@/views/candidat/CvAccueilView.vue'), meta: { permissions: ['DOCUMENT_CONSULTER_PERSO'] } },
      { path: 'cv/creer', name: 'candidat-cv-creer', component: () => import('@/views/candidat/CvCreerView.vue'), meta: { permissions: ['DOCUMENT_CONSULTER_PERSO'] } },
      { path: 'cv/importer', name: 'candidat-cv-importer', component: () => import('@/views/candidat/CvImporterView.vue'), meta: { permissions: ['DOCUMENT_CONSULTER_PERSO'] } },
      { path: 'offres', name: 'candidat-offres', redirect: { name: 'offres-publiques' } },
      { path: 'candidature', name: 'candidat-candidature', component: () => import('@/views/candidat/CandidatureView.vue'), meta: { permissions: ['CANDIDATURE_POSTULER'] } },
      { path: 'propositions', name: 'candidat-propositions', component: () => import('@/views/candidat/PropositionsView.vue'), meta: { permissions: ['PROPOSITION_REPONDRE'] } },
    ],
  },
  { path: '/profil/mot-de-passe', name: 'changer-mot-de-passe', component: () => import('@/views/profil/ChangerMotDePasseView.vue'), meta: { requiresAuth: true, espace: 'Mon profil' } },
  { path: '/notifications', name: 'notifications', component: () => import('@/views/NotificationsView.vue'), meta: { requiresAuth: true, espace: 'Notifications' } },
  {
    path: '/direction', component: DirectionLayout, meta: { requiresAuth: true, roles: ['DIRECTION'], espace: 'Direction' },
    children: [
      { path: '', redirect: { name: 'direction-dashboard' } },
      { path: 'dashboard', name: 'direction-dashboard', component: () => import('@/views/direction/DashboardView.vue'), meta: { permissions: ['REPORTING_CONSULTER'] } },
      { path: 'arbitrage', name: 'direction-arbitrage', component: () => import('@/views/direction/ArbitrageView.vue'), meta: { permissions: ['DEMANDE_APPROUVER_DIR'] } },
      { path: 'demandes/:id', name: 'direction-demande-arbitrage', component: () => import('@/views/direction/DemandeArbitrageView.vue'), meta: { permissions: ['DEMANDE_APPROUVER_DIR'] } },
    ],
  },
  { path: '/salarie/accueil', name: 'salarie-accueil', component: () => import('@/views/salarie/AccueilView.vue'), meta: { requiresAuth: true, roles: ['SALARIE'], espace: 'Espace salarié' } },
  { path: '/salarie/contrats', name: 'salarie-contrats', component: () => import('@/views/salarie/ContratsView.vue'), meta: { requiresAuth: true, roles: ['SALARIE'], permissions: ['DOCUMENT_CONSULTER_PERSO'], espace: 'Mes contrats' } },
  { path: '/admin/utilisateurs', name: 'admin-utilisateurs', component: () => import('@/views/admin/UtilisateursView.vue'), meta: { requiresAuth: true, roles: ['ADMIN'], espace: 'Administration' } },
  { path: '/taches', name: 'mes-taches', component: () => import('@/views/taches/TachesView.vue'), meta: { requiresAuth: true, roles: ['MANAGER', 'GESTIONNAIRE_RH', 'SALARIE', 'IT', 'MOYENS_GENERAUX'], permissions: ['TACHE_TRAITER'], espace: 'Mes tâches' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { public: true } },
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.name === 'home' && !auth.isAuthenticated) return { name: 'login' }
  if (to.meta.public) return true
  if (auth.isAuthenticated) await auth.chargerUtilisateur()
  if (!auth.isAuthenticated || !auth.utilisateur) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'home') return auth.homeRoute
  const roles = to.matched.flatMap((record) => record.meta.roles ?? [])
  if (roles.length && !roles.some((role) => auth.hasRole(role))) return { name: 'forbidden', query: { redirect: to.fullPath } }
  const permissions = to.matched.flatMap((record) => record.meta.permissions ?? [])
  if (permissions.length && !auth.hasAnyPermission(permissions)) return { name: 'forbidden', query: { redirect: to.fullPath } }
  return true
})

export default router
