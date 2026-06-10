<template>
  <section class="page-view narrow-page">
    <header class="page-heading"><div><h1>账号设置</h1><p>修改登录密码或退出当前账号。</p></div></header>
    <el-card shadow="never">
      <el-form label-width="100px">
        <el-form-item label="旧密码"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item>
        <el-form-item><el-button type="primary" @click="changePassword">修改密码</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="page-card" shadow="never"><h3>登录状态</h3><p>退出后会清理当前浏览器中的 JWT 和用户资料缓存。</p><el-button type="danger" plain @click="logout">退出登录</el-button></el-card>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usersApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
const router = useRouter()
const auth = useAuthStore()
const form = reactive({ oldPassword: '', newPassword: '' })
async function changePassword() { await usersApi.password(form); form.oldPassword = ''; form.newPassword = ''; ElMessage.success('密码已修改') }
async function logout() { await auth.logout(); router.push('/login') }
</script>
