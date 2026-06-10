<template>
  <section class="page-view narrow-page">
    <header class="page-heading"><div><h1>个人资料</h1><p>用户 ID 和登录用户名不可修改。</p></div></header>
    <el-card shadow="never">
      <el-form label-width="100px" class="profile-form">
        <el-form-item label="头像"><div class="avatar-editor"><el-avatar :size="88" :src="assetUrl(form.avatarUrl)">{{ displayInitial(form) }}</el-avatar><el-upload :show-file-list="false" accept="image/jpeg,image/png,image/gif,image/webp" :before-upload="uploadAvatar"><el-button :loading="uploading">选择图片</el-button></el-upload><small>支持 JPG、PNG、GIF、WebP，最大 5MB</small></div></el-form-item>
        <el-form-item label="用户 ID"><el-input :model-value="auth.user?.id" disabled /></el-form-item>
        <el-form-item label="用户名"><el-input :model-value="auth.user?.username" disabled /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.bio" type="textarea" :rows="4" /></el-form-item>
        <el-form-item><el-button type="primary" @click="save">保存资料</el-button></el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { usersApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { assetUrl, displayInitial } from '../utils/display'
const auth = useAuthStore()
const form = reactive({ nickname: '', avatarUrl: '', bio: '' })
const uploading = ref(false)
onMounted(() => Object.assign(form, auth.user || {}))
async function save() {
  auth.user = await usersApi.update({ nickname: form.nickname, avatarUrl: form.avatarUrl, bio: form.bio })
  auth.persist()
  Object.assign(form, auth.user)
  ElMessage.success('资料已保存')
}
async function uploadAvatar(file) {
  if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type) || file.size > 5 * 1024 * 1024) { ElMessage.error('请选择不超过 5MB 的 JPG、PNG、GIF 或 WebP 图片'); return false }
  uploading.value = true
  try { auth.user = await usersApi.uploadAvatar(file); auth.persist(); Object.assign(form, auth.user); ElMessage.success('头像上传成功') } finally { uploading.value = false }
  return false
}
</script>
