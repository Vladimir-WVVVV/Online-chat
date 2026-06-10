<template>
  <section class="page-view">
    <header class="page-heading"><div><h1>管理员后台</h1><p>查看系统指标并管理用户状态。</p></div></header>
    <div class="metric-grid">
      <el-card shadow="never"><strong>在线人数</strong><h2>{{ metrics.onlineCount }}</h2></el-card>
      <el-card shadow="never"><strong>今日消息</strong><h2>{{ metrics.todayMessageCount }}</h2></el-card>
      <el-card shadow="never"><strong>今日新增</strong><h2>{{ metrics.todayNewUserCount }}</h2></el-card>
    </div>
    <el-card class="page-card" shadow="never">
      <template #header><strong>用户列表</strong></template>
      <div v-for="user in users" :key="user.id" class="member-row">
        <span><strong>{{ user.nickname || user.username }}</strong><small>@{{ user.username }} · ID {{ user.id }} · {{ user.status }} · {{ user.role }}</small></span>
        <span>
          <el-button v-if="user.role !== 'ADMIN' && user.status !== 'BANNED'" link type="danger" @click="ban(user.id)">封禁</el-button>
          <el-button v-if="user.role !== 'ADMIN' && user.status === 'BANNED'" link @click="unban(user.id)">解封</el-button>
        </span>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { adminApi } from '../api/modules'
const metrics = reactive({ onlineCount: 0, todayMessageCount: 0, todayNewUserCount: 0 })
const users = ref([])
onMounted(load)
async function load() { Object.assign(metrics, await adminApi.metrics()); users.value = await adminApi.users() }
async function ban(id) { await ElMessageBox.confirm(`确定封禁用户 ID ${id}？`, '管理员操作'); await adminApi.ban(id); await load() }
async function unban(id) { await adminApi.unban(id); await load() }
</script>
