<template>
  <div class="system-shell">
    <header class="system-header">
      <router-link class="system-brand" to="/app/chat">OnlineChat</router-link>
      <div class="current-user">
        <el-avatar :size="36" :src="assetUrl(auth.user?.avatarUrl)">{{ displayInitial(auth.user) }}</el-avatar>
        <span>
          <strong>{{ displayName(auth.user) }}</strong>
          <small>@{{ auth.user?.username }} · ID {{ auth.user?.id }} · {{ auth.user?.status }}</small>
        </span>
        <el-button size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <aside class="system-nav">
      <router-link v-for="item in visibleNavItems" :key="item.path" :to="item.path" class="nav-item">
        {{ item.label }}
      </router-link>
    </aside>

    <main class="system-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { assetUrl, displayInitial, displayName } from '../utils/display'

const router = useRouter()
const auth = useAuthStore()
const navItems = [
  { path: '/app/chat', label: '聊天' },
  { path: '/app/friends', label: '好友' },
  { path: '/app/groups', label: '群聊' },
  { path: '/app/notifications', label: '通知' },
  { path: '/app/profile', label: '个人资料' },
  { path: '/app/settings', label: '设置' },
  { path: '/app/admin', label: '后台', adminOnly: true }
]
const visibleNavItems = computed(() => navItems.filter((item) => !item.adminOnly || auth.user?.role === 'ADMIN'))

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>
