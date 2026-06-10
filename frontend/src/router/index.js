import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ChatHomeView from '../views/ChatHomeView.vue'
import AppLayout from '../layouts/AppLayout.vue'
import FriendsView from '../views/FriendsView.vue'
import GroupsView from '../views/GroupsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import ProfileView from '../views/ProfileView.vue'
import SettingsView from '../views/SettingsView.vue'
import AdminView from '../views/AdminView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/app/chat' },
    { path: '/login', component: LoginView, meta: { guestOnly: true } },
    { path: '/register', component: RegisterView, meta: { guestOnly: true } },
    { path: '/chat', redirect: '/app/chat' },
    {
      path: '/app',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/app/chat' },
        { path: 'chat', component: ChatHomeView },
        { path: 'friends', component: FriendsView },
        { path: 'groups', component: GroupsView },
        { path: 'notifications', component: NotificationsView },
        { path: 'profile', component: ProfileView },
        { path: 'settings', component: SettingsView },
        { path: 'admin', component: AdminView, meta: { adminOnly: true } }
      ]
    }
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
    return '/app/chat'
  }
  if (to.meta.adminOnly && auth.user?.role !== 'ADMIN') {
    return '/app/chat'
  }
  return true
})

export default router
