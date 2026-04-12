import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { initAuth } from './auth'
import './styles/global.css'

initAuth()
createApp(App).use(router).mount('#app')
