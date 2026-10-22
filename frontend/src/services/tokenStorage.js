// Stockage du jeton d'authentification (isolé pour éviter les imports circulaires store <-> http).
const KEY = 'gestionrh.token'

export const tokenStorage = {
  get() {
    try { return localStorage.getItem(KEY) } catch { return null }
  },
  set(token) {
    try { localStorage.setItem(KEY, token) } catch { /* stockage indisponible */ }
  },
  clear() {
    try { localStorage.removeItem(KEY) } catch { /* stockage indisponible */ }
  },
}
