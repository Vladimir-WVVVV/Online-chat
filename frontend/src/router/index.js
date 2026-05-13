import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ChatHomeView from '../views/ChatHomeView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/chat' },
    { path: '/login', component: LoginView, meta: { guestOnly: true } },
    { path: '/register', component: RegisterView, meta: { guestOnly: true } },
    { path: '/chat', component: ChatHomeView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.loaded) {
    await auth.fetchMe()
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }
  if (to.meta.guestOnly && auth.isLoggedIn) {
    return '/chat'
  }
  return true
})

export default router

