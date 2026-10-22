import { createApp } from 'vue'
import { createPinia } from 'pinia'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './assets/main.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import permissions from './plugins/permissions'

const app = createApp(App)
const pinia = createPinia()
async function bootstrap() {
  app.use(pinia)
  await useAuthStore(pinia).chargerUtilisateur()
  app.use(router)
  app.directive('can', permissions)
  app.mount('#app')
}

bootstrap()
