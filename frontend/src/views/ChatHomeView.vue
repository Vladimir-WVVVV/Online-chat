<template>
  <main class="app-shell">
    <aside class="sidebar">
      <header class="panel-header">
        <div>
          <h2>OnlineChat</h2>
          <span>{{ auth.user?.nickname || auth.user?.username }}</span>
        </div>
        <el-button size="small" @click="handleLogout">退出</el-button>
      </header>
      <el-tabs model-value="friends" stretch>
        <el-tab-pane label="好友" name="friends">
          <el-empty description="好友模块将在第 4 步实现" />
        </el-tab-pane>
        <el-tab-pane label="群聊" name="groups">
          <el-empty description="群聊模块将在第 6 步实现" />
        </el-tab-pane>
      </el-tabs>
    </aside>

    <section class="chat-area">
      <header class="panel-header">
        <h3>聊天主界面</h3>
        <el-tag type="success">已登录</el-tag>
      </header>
      <div class="message-list">
        <div class="empty-state">
          注册、登录、JWT 鉴权与路由守卫闭环已完成。
        </div>
      </div>
      <footer class="composer">
        <el-input
          type="textarea"
          :rows="3"
          disabled
          placeholder="单聊 WebSocket 将在第 5 步接入"
        />
      </footer>
    </section>

    <aside class="info-panel">
      <header class="panel-header">
        <h3>个人信息</h3>
      </header>
      <el-descriptions :column="1" border style="margin: 16px">
        <el-descriptions-item label="用户名">{{ auth.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ auth.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ auth.user?.role }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ auth.user?.status }}</el-descriptions-item>
      </el-descriptions>
    </aside>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

