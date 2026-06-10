<template>
  <section class="page-view narrow-page">
    <header class="page-heading"><div><h1>通知中心</h1><p>查看系统通知并处理好友申请。</p></div><el-button @click="readAll">全部已读</el-button></header>
    <el-card shadow="never">
      <div v-for="item in notifications" :key="item.id" class="notice-card" :class="{ unread: !item.read }">
        <strong>{{ item.type }}</strong><p>{{ item.content }}</p>
        <el-button v-if="!item.read" link @click="read(item.id)">标记已读</el-button>
      </div>
      <el-empty v-if="!notifications.length" description="暂无通知" />
    </el-card>
    <el-card class="page-card" shadow="never">
      <template #header><strong>待处理好友申请</strong></template>
      <div v-for="item in requests" :key="item.id" class="member-row">
        <span>{{ item.fromNickname || item.fromUsername }} · @{{ item.fromUsername }} · ID {{ item.fromUserId }}</span>
        <span v-if="item.status === 'PENDING'"><el-button @click="accept(item.id)">接受</el-button><el-button @click="reject(item.id)">拒绝</el-button></span>
        <span v-else>{{ item.status }}</span>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { friendsApi, notificationsApi } from '../api/modules'
const notifications = ref([])
const requests = ref([])
onMounted(load)
async function load() { notifications.value = await notificationsApi.list(); requests.value = await friendsApi.received() }
async function read(id) { await notificationsApi.read(id); await load() }
async function readAll() { await notificationsApi.readAll(); await load() }
async function accept(id) { await friendsApi.accept(id); await load() }
async function reject(id) { await friendsApi.reject(id); await load() }
</script>
