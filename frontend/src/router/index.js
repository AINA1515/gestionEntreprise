import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import ManagerLayout from '@/components/layout/ManagerLayout.vue'
import RhLayout from '@/components/layout/RhLayout.vue'
import PageLayout from '@/components/layout/PageLayout.vue'

const AUTH_ENABLED = import.meta.env.VITE_AUTH_ENABLED === 'true'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
  { path: '/inscription', name: 'inscription', component: () => import('@/views/auth/RegisterView.vue') },

  // Espace manager (barre latérale)
  {
    path: '/manager',
    component: ManagerLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: { name: 'manager-dashboard' } },
      { path: 'dashboard', name: 'manager-dashboard', component: () => import('@/views/manager/DashboardView.vue') },
      { path: 'demandes', name: 'manager-demandes', component: () => import('@/views/manager/MesDemandesView.vue') },
      { path: 'demandes/nouvelle', name: 'manager-nouvelle-demande', component: () => import('@/views/manager/NouvelleDemandeView.vue') },
    ],
  },

  // Espace RH : suivi (barre latérale)
  {
    path: '/rh',
    component: RhLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: { name: 'rh-candidatures' } },
      { path: 'candidatures', name: 'rh-candidatures', component: () => import('@/views/rh/CandidaturesView.vue') },
    ],
  },

  // Espace RH : pages de saisie (en-tête + retour)
  {
    path: '/rh',
    component: PageLayout,
    meta: {
      requiresAuth: true,
      espace: 'Espace Ressources humaines',
      retour: { to: '/rh/candidatures', label: 'Retour aux candidatures' },
    },
    children: [
      { path: 'decision', name: 'rh-decision', component: () => import('@/views/rh/DecisionView.vue') },
      { path: 'confirmation', name: 'rh-confirmation', component: () => import('@/views/rh/ConfirmationView.vue') },
    ],
  },

  // Espace candidat (en-tête)
  {
    path: '/candidat',
    component: PageLayout,
    meta: { requiresAuth: true, espace: 'Espace candidat' },
    children: [
      { path: '', redirect: { name: 'candidat-cv' } },
      { path: 'cv', name: 'candidat-cv', component: () => import('@/views/candidat/CvAccueilView.vue') },
      { path: 'cv/creer', name: 'candidat-cv-creer', component: () => import('@/views/candidat/CvCreerView.vue') },
      { path: 'cv/importer', name: 'candidat-cv-importer', component: () => import('@/views/candidat/CvImporterView.vue') },
      { path: 'offres', name: 'candidat-offres', component: () => import('@/views/candidat/OffresView.vue') },
      { path: 'candidature', name: 'candidat-candidature', component: () => import('@/views/candidat/CandidatureView.vue') },
    ],
  },

  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// Garde : activée par défaut dans l'environnement d'exemple.
router.beforeEach(async (to) => {
  if (!AUTH_ENABLED) return true
  if (to.name === 'login' || to.name === 'inscription') return true
  const auth = useAuthStore()
  if (to.matched.some((r) => r.meta.requiresAuth)) {
    if (auth.isAuthenticated) await auth.chargerUtilisateur()
    if (!auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
