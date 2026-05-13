<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="brand">
        <h1>OnlineChat</h1>
        <p>登录后进入课程演示聊天室。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" size="large" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <div class="form-actions">
          <el-button link type="primary" @click="$router.push('/register')">注册账号</el-button>
          <el-button type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
        </div>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'alice',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push('/chat')
  } finally {
    loading.value = false
  }
}
</script>

