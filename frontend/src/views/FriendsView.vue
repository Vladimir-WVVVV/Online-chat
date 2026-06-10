<template>
  <section class="page-view">
    <header class="page-heading">
      <div><h1>好友管理</h1><p>搜索用户、处理好友申请并查看好友唯一 ID。</p></div>
      <el-button type="primary" @click="searchUsers">搜索用户</el-button>
    </header>

    <div class="content-grid two-column">
      <el-card shadow="never">
        <template #header><strong>好友列表</strong></template>
        <button v-for="friend in friends" :key="friend.id" class="entity-row" @click="selected = friend">
          <el-avatar :src="assetUrl(friend.avatarUrl)">{{ displayInitial(friend) }}</el-avatar>
          <span><strong>{{ displayName(friend) }}</strong><small>@{{ friend.username }} · ID {{ friend.id }} · {{ friend.status }}</small></span>
          <el-badge v-if="friend.unreadCount" :value="friend.unreadCount" />
        </button>
        <el-empty v-if="!friends.length" description="暂无好友" />
      </el-card>

      <el-card shadow="never">
        <template #header><strong>好友详情</strong></template>
        <div v-if="selected" class="detail-card">
          <el-avatar :size="72" :src="assetUrl(selected.avatarUrl)">{{ displayInitial(selected) }}</el-avatar>
          <h2>{{ displayName(selected) }}</h2>
          <p>@{{ selected.username }}</p><p>用户 ID：{{ selected.id }}</p><p>{{ selected.bio || '暂无简介' }}</p>
          <el-button type="primary" @click="chatWith(selected.id)">发起聊天</el-button>
          <el-button type="danger" plain @click="deleteFriend(selected.id)">删除好友</el-button>
        </div>
        <el-empty v-else description="选择好友查看详情" />
      </el-card>
    </div>

    <el-card class="page-card" shadow="never">
      <template #header><strong>添加好友</strong></template>
      <div class="inline-form">
        <el-input v-model="keyword" placeholder="输入 username 或 nickname" clearable @keyup.enter="searchUsers" />
        <el-button type="primary" @click="searchUsers">搜索</el-button>
      </div>
      <div v-for="user in searchResults" :key="user.id" class="member-row">
        <span><strong>{{ user.nickname || user.username }}</strong><small>@{{ user.username }} · 用户 ID {{ user.id }} · {{ user.status }}</small></span>
        <el-button @click="sendRequest(user.id)">添加 ID {{ user.id }}</el-button>
      </div>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header><strong>好友申请</strong></template>
      <el-tabs>
        <el-tab-pane label="收到的">
          <div v-for="item in received" :key="item.id" class="member-row">
            <span>{{ item.fromNickname || item.fromUsername }} · @{{ item.fromUsername }} · ID {{ item.fromUserId }} · {{ item.status }}</span>
            <span v-if="item.status === 'PENDING'"><el-button @click="accept(item.id)">接受</el-button><el-button @click="reject(item.id)">拒绝</el-button></span>
          </div>
        </el-tab-pane>
        <el-tab-pane label="发出的">
          <div v-for="item in sent" :key="item.id" class="member-row">
            <span>{{ item.toNickname || item.toUsername }} · @{{ item.toUsername }} · ID {{ item.toUserId }} · {{ item.status }}</span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { friendsApi, usersApi } from '../api/modules'
import { assetUrl, displayInitial, displayName } from '../utils/display'

const router = useRouter()
const friends = ref([])
const selected = ref(null)
const keyword = ref('')
const searchResults = ref([])
const received = ref([])
const sent = ref([])

onMounted(loadAll)
async function loadAll() {
  friends.value = await friendsApi.list()
  received.value = await friendsApi.received()
  sent.value = await friendsApi.sent()
}
async function searchUsers() { searchResults.value = await usersApi.search(keyword.value) }
async function sendRequest(toUserId) { await friendsApi.request({ toUserId, message: '请求添加好友' }); ElMessage.success('好友申请已发送') }
async function accept(id) { await friendsApi.accept(id); await loadAll() }
async function reject(id) { await friendsApi.reject(id); await loadAll() }
async function deleteFriend(id) { await ElMessageBox.confirm('确定删除该好友？', '删除好友'); await friendsApi.delete(id); selected.value = null; await loadAll() }
function chatWith(id) { router.push({ path: '/app/chat', query: { type: 'PRIVATE', id } }) }
</script>
