import { onMounted, onUnmounted } from 'vue'

export function usePolling(callback, interval = 60_000) {
  let timer = null
  async function tick() {
    if (document.visibilityState !== 'visible') return
    try { await callback() } catch { /* Le service associé signale déjà les erreurs utiles. */ }
  }
  function start() {
    if (timer) window.clearInterval(timer)
    timer = null
    if (document.visibilityState === 'visible') timer = window.setInterval(tick, interval)
  }
  function visibilityChanged() { start(); if (document.visibilityState === 'visible') tick() }
  onMounted(() => { document.addEventListener('visibilitychange', visibilityChanged); tick(); start() })
  onUnmounted(() => { document.removeEventListener('visibilitychange', visibilityChanged); if (timer) window.clearInterval(timer) })
  return { refresh: tick }
}
