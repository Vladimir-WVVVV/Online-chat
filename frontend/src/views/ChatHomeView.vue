<template>
  <main class="app-shell">
    <aside class="sidebar">
      <header class="panel-header">
        <div>
          <h2>OnlineChat</h2>
          <span>{{ auth.user?.nickname || auth.user?.username }} · {{ auth.user?.status }}</span>
        </div>
        <el-button size="small" @click="handleLogout">退出</el-button>
      </header>

      <div class="toolbar-row">
        <el-button size="small" @click="openAddFriend">添加好友</el-button>
        <el-button size="small" @click="requestDialog = true">好友申请</el-button>
        <el-button size="small" @click="groupDialog = true">创建群聊</el-button>
      </div>

      <el-tabs v-model="leftTab" stretch class="left-tabs">
        <el-tab-pane label="好友" name="friends">
          <button
            v-for="friend in friends"
            :key="friend.id"
            class="list-item"
            :class="{ active: activeTarget?.type === 'PRIVATE' && activeTarget.id === friend.id }"
            @click="openPrivate(friend)"
          >
            <span>
              <strong>{{ friend.remark || friend.nickname }}</strong>
              <small>{{ friend.username }} · {{ friend.status }}</small>
            </span>
            <el-badge v-if="friend.unreadCount" :value="friend.unreadCount" />
          </button>
          <el-empty v-if="!friends.length" description="暂无好友" />
        </el-tab-pane>
        <el-tab-pane label="群聊" name="groups">
          <div class="join-group-row">
            <el-input v-model="joinGroupId" size="small" placeholder="输入群 ID 加入" />
            <el-button size="small" @click="joinGroup">加入</el-button>
          </div>
          <button
            v-for="group in groups"
            :key="group.id"
            class="list-item"
            :class="{ active: activeTarget?.type === 'GROUP' && activeTarget.id === group.id }"
            @click="openGroup(group)"
          >
            <span>
              <strong>{{ group.name }}</strong>
              <small>{{ group.memberCount }} 人 · 群主 {{ group.ownerName }}</small>
            </span>
            <el-badge v-if="group.unreadCount" :value="group.unreadCount" />
          </button>
          <el-empty v-if="!groups.length" description="暂无群聊" />
        </el-tab-pane>
      </el-tabs>
    </aside>

    <section class="chat-area">
      <header class="panel-header">
        <div>
          <h3>{{ activeTitle }}</h3>
          <span>{{ connected ? '实时连接已建立' : '正在等待连接' }}</span>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" size="small" placeholder="搜索当前历史" clearable @keyup.enter="loadHistory" />
          <el-button size="small" @click="loadHistory">搜索</el-button>
        </div>
      </header>

      <div class="message-list">
        <el-button v-if="activeTarget" size="small" text @click="loadMore">加载更早消息</el-button>
        <div v-if="!activeTarget" class="empty-state">选择好友或群聊开始课堂演示</div>
        <article
          v-for="message in orderedMessages"
          :key="message.messageId"
          class="message-row"
          :class="{ mine: message.senderId === auth.user?.id }"
        >
          <div class="bubble">
            <div class="message-meta">{{ message.senderName }} · {{ formatTime(message.createTime) }}</div>
            <template v-if="message.recalled">
              <em>消息已撤回</em>
            </template>
            <template v-else-if="message.messageType === 'IMAGE'">
              <img v-if="filePreviews[message.fileId]" class="chat-image" :src="filePreviews[message.fileId]" alt="图片消息" />
              <div>{{ message.content }}</div>
              <el-button size="small" @click="downloadFile(message.fileId)">下载</el-button>
            </template>
            <template v-else-if="message.messageType === 'FILE'">
              <div class="file-message">{{ message.content }}</div>
              <el-button size="small" @click="downloadFile(message.fileId)">下载文件</el-button>
            </template>
            <template v-else>{{ message.content }}</template>
            <el-button
              v-if="canRecall(message)"
              class="recall-button"
              size="small"
              link
              @click="recall(message)"
            >
              撤回
            </el-button>
          </div>
        </article>
      </div>

      <footer class="composer">
        <el-upload :show-file-list="false" :before-upload="uploadAndSend">
          <el-button>文件</el-button>
        </el-upload>
        <el-input v-model="draft" type="textarea" :rows="3" :disabled="!activeTarget" placeholder="输入消息，Enter 发送" @keydown.enter.exact.prevent="sendText" />
        <el-button type="primary" :disabled="!activeTarget || !draft.trim()" @click="sendText">发送</el-button>
      </footer>
    </section>

    <aside class="info-panel">
      <el-tabs v-model="rightTab" stretch>
        <el-tab-pane label="资料" name="profile">
          <div class="side-section">
            <el-form label-width="72px">
              <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
              <el-form-item label="头像"><el-input v-model="profile.avatarUrl" /></el-form-item>
              <el-form-item label="简介"><el-input v-model="profile.bio" type="textarea" /></el-form-item>
              <el-button type="primary" @click="saveProfile">保存资料</el-button>
            </el-form>
            <el-divider />
            <el-form label-width="72px">
              <el-form-item label="旧密码"><el-input v-model="password.oldPassword" show-password /></el-form-item>
              <el-form-item label="新密码"><el-input v-model="password.newPassword" show-password /></el-form-item>
              <el-button @click="changePassword">修改密码</el-button>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="通知" name="notifications">
          <div class="side-section">
            <el-button size="small" @click="readAll">全部已读</el-button>
            <div v-for="item in notifications" :key="item.id" class="notice">
              <strong>{{ item.type }}</strong>
              <p>{{ item.content }}</p>
              <el-button v-if="!item.read" size="small" link @click="readNotice(item.id)">标记已读</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="群成员" name="members">
          <div class="side-section">
            <div v-for="member in members" :key="member.userId" class="member-row">
              <span>{{ member.nickname }} · {{ member.role }}</span>
              <el-button v-if="activeGroupOwner && member.role !== 'OWNER'" size="small" link @click="removeMember(member.userId)">移除</el-button>
            </div>
            <el-button v-if="activeTarget?.type === 'GROUP'" size="small" @click="leaveGroup">退出群聊</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="auth.user?.role === 'ADMIN'" label="后台" name="admin">
          <div class="side-section">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="在线人数">{{ metrics.onlineCount }}</el-descriptions-item>
              <el-descriptions-item label="今日消息">{{ metrics.todayMessageCount }}</el-descriptions-item>
              <el-descriptions-item label="今日新增">{{ metrics.todayNewUserCount }}</el-descriptions-item>
            </el-descriptions>
            <div v-for="user in adminUsers" :key="user.id" class="member-row">
              <span>{{ user.username }} · {{ user.status }} · {{ user.role }}</span>
              <el-button v-if="user.role !== 'ADMIN' && user.status !== 'BANNED'" size="small" link @click="ban(user.id)">封禁</el-button>
              <el-button v-if="user.role !== 'ADMIN' && user.status === 'BANNED'" size="small" link @click="unban(user.id)">解封</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </aside>

    <el-dialog v-model="addFriendDialog" title="添加好友" width="520px">
      <el-input v-model="searchKeyword" placeholder="输入用户名或昵称" @keyup.enter="searchUsers" />
      <el-button style="margin-top: 12px" @click="searchUsers">搜索</el-button>
      <div v-for="user in searchResults" :key="user.id" class="member-row">
        <span>{{ user.nickname }} · {{ user.username }} · {{ user.status }}</span>
        <el-button size="small" @click="sendFriendRequest(user.id)">申请</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="requestDialog" title="好友申请" width="680px" @open="loadRequests">
      <el-tabs>
        <el-tab-pane label="收到的">
          <div v-for="item in receivedRequests" :key="item.id" class="member-row">
            <span>{{ item.fromNickname }}：{{ item.message || '请求添加好友' }} · {{ item.status }}</span>
            <span v-if="item.status === 'PENDING'">
              <el-button size="small" @click="acceptRequest(item.id)">接受</el-button>
              <el-button size="small" @click="rejectRequest(item.id)">拒绝</el-button>
            </span>
          </div>
        </el-tab-pane>
        <el-tab-pane label="发出的">
          <div v-for="item in sentRequests" :key="item.id" class="member-row">
            <span>给 {{ item.toNickname }} · {{ item.status }}</span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog v-model="groupDialog" title="创建群聊" width="480px">
      <el-form label-width="72px">
        <el-form-item label="群名"><el-input v-model="newGroup.name" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="newGroup.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog = false">取消</el-button>
        <el-button type="primary" @click="createGroup">创建</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import axios from 'axios'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { adminApi, filesApi, friendsApi, groupsApi, messagesApi, notificationsApi, usersApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { createStompClient } from '../websocket/client'

const router = useRouter()
const auth = useAuthStore()
const leftTab = ref('friends')
const rightTab = ref('profile')
const friends = ref([])
const groups = ref([])
const notifications = ref([])
const members = ref([])
const messages = ref([])
const page = ref(1)
const draft = ref('')
const keyword = ref('')
const activeTarget = ref(null)
const connected = ref(false)
const stomp = ref(null)
const filePreviews = reactive({})
const addFriendDialog = ref(false)
const requestDialog = ref(false)
const groupDialog = ref(false)
const joinGroupId = ref('')
const searchKeyword = ref('')
const searchResults = ref([])
const receivedRequests = ref([])
const sentRequests = ref([])
const newGroup = reactive({ name: '', description: '' })
const profile = reactive({ nickname: '', avatarUrl: '', bio: '' })
const password = reactive({ oldPassword: '', newPassword: '' })
const metrics = reactive({ onlineCount: 0, todayMessageCount: 0, todayNewUserCount: 0 })
const adminUsers = ref([])

const activeTitle = computed(() => activeTarget.value ? activeTarget.value.name : '聊天主界面')
const orderedMessages = computed(() => [...messages.value].sort((a, b) => new Date(a.createTime) - new Date(b.createTime)))
const activeGroupOwner = computed(() => activeTarget.value?.type === 'GROUP' && activeTarget.value.ownerId === auth.user?.id)

onMounted(async () => {
  Object.assign(profile, auth.user || {})
  await reloadAll()
  connectSocket()
})

onUnmounted(() => {
  if (stomp.value) stomp.value.deactivate()
})

async function reloadAll() {
  friends.value = await friendsApi.list()
  groups.value = await groupsApi.list()
  notifications.value = await notificationsApi.list()
  if (auth.user?.role === 'ADMIN') await loadAdmin()
}

function connectSocket() {
  stomp.value = createStompClient(auth.token, {
    onConnect: () => {
      connected.value = true
      stomp.value.subscribe('/user/queue/messages', (frame) => handleIncoming(JSON.parse(frame.body)))
      stomp.value.subscribe('/user/queue/notifications', async (frame) => {
        notifications.value.unshift(JSON.parse(frame.body))
        await reloadBadges()
        ElMessage.info('收到新通知')
      })
      stomp.value.subscribe('/topic/online', reloadBadges)
      for (const group of groups.value) subscribeGroup(group.id)
    },
    onDisconnect: () => { connected.value = false },
    onError: () => { connected.value = false }
  })
  stomp.value.activate()
}

function subscribeGroup(groupId) {
  if (stomp.value?.connected) {
    stomp.value.subscribe(`/topic/groups/${groupId}`, (frame) => handleIncoming(JSON.parse(frame.body)))
  }
}

async function handleIncoming(message) {
  upsertMessage(message)
  await ensurePreview(message)
  if (activeTarget.value?.type === 'PRIVATE' && message.senderId === activeTarget.value.id) {
    await messagesApi.readPrivate(activeTarget.value.id)
  }
  if (activeTarget.value?.type === 'GROUP' && message.groupId === activeTarget.value.id) {
    await messagesApi.readGroup(activeTarget.value.id)
  }
  await reloadBadges()
}

function upsertMessage(message) {
  const index = messages.value.findIndex((item) => item.messageId === message.messageId)
  if (index >= 0) messages.value[index] = message
  else if (isForActive(message)) messages.value.push(message)
  nextTick(() => document.querySelector('.message-list')?.scrollTo({ top: 999999 }))
}

function isForActive(message) {
  if (!activeTarget.value) return false
  if (activeTarget.value.type === 'GROUP') return message.groupId === activeTarget.value.id
  return message.conversationType === 'PRIVATE' && (message.senderId === activeTarget.value.id || message.receiverId === activeTarget.value.id)
}

async function reloadBadges() {
  friends.value = await friendsApi.list()
  groups.value = await groupsApi.list()
  notifications.value = await notificationsApi.list()
}

async function openPrivate(friend) {
  activeTarget.value = { type: 'PRIVATE', id: friend.id, name: friend.remark || friend.nickname }
  page.value = 1
  await loadHistory()
  await messagesApi.readPrivate(friend.id)
  await reloadBadges()
}

async function openGroup(group) {
  activeTarget.value = { type: 'GROUP', id: group.id, name: group.name, ownerId: group.ownerId }
  rightTab.value = 'members'
  page.value = 1
  await loadHistory()
  members.value = await groupsApi.members(group.id)
  await messagesApi.readGroup(group.id)
  subscribeGroup(group.id)
  await reloadBadges()
}

async function loadHistory() {
  if (!activeTarget.value) return
  const params = { page: page.value, size: 20, keyword: keyword.value }
  const data = activeTarget.value.type === 'PRIVATE'
    ? await messagesApi.privateHistory(activeTarget.value.id, params)
    : await messagesApi.groupHistory(activeTarget.value.id, params)
  messages.value = page.value === 1 ? data : [...messages.value, ...data]
  for (const message of messages.value) await ensurePreview(message)
}

async function loadMore() {
  page.value += 1
  await loadHistory()
}

function sendText() {
  if (!draft.value.trim() || !activeTarget.value || !stomp.value?.connected) return
  const payload = { content: draft.value.trim(), messageType: 'TEXT' }
  if (activeTarget.value.type === 'PRIVATE') {
    stomp.value.publish({ destination: '/app/chat.private', body: JSON.stringify({ ...payload, receiverId: activeTarget.value.id }) })
  } else {
    stomp.value.publish({ destination: '/app/chat.group', body: JSON.stringify({ ...payload, groupId: activeTarget.value.id }) })
  }
  draft.value = ''
}

async function uploadAndSend(file) {
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('单文件不能超过 20MB')
    return false
  }
  const record = await filesApi.upload(file)
  const type = file.type?.startsWith('image/') ? 'IMAGE' : 'FILE'
  const payload = { content: record.originalName, messageType: type, fileId: record.id }
  if (activeTarget.value?.type === 'PRIVATE') {
    stomp.value.publish({ destination: '/app/chat.private', body: JSON.stringify({ ...payload, receiverId: activeTarget.value.id }) })
  } else if (activeTarget.value?.type === 'GROUP') {
    stomp.value.publish({ destination: '/app/chat.group', body: JSON.stringify({ ...payload, groupId: activeTarget.value.id }) })
  }
  return false
}

async function ensurePreview(message) {
  if (message.messageType !== 'IMAGE' || !message.fileId || filePreviews[message.fileId]) return
  const response = await axios.get(filesApi.downloadUrl(message.fileId), {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${auth.token}` }
  })
  filePreviews[message.fileId] = URL.createObjectURL(response.data)
}

async function downloadFile(id) {
  const response = await axios.get(filesApi.downloadUrl(id), {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${auth.token}` }
  })
  const url = URL.createObjectURL(response.data)
  const a = document.createElement('a')
  a.href = url
  a.download = ''
  a.click()
  URL.revokeObjectURL(url)
}

function canRecall(message) {
  return message.senderId === auth.user?.id && !message.recalled && Date.now() - new Date(message.createTime).getTime() < 120000
}

async function recall(message) {
  await messagesApi.recall(message.messageId)
}

function openAddFriend() {
  searchResults.value = []
  addFriendDialog.value = true
}

async function searchUsers() {
  searchResults.value = await usersApi.search(searchKeyword.value)
}

async function sendFriendRequest(toUserId) {
  await friendsApi.request({ toUserId, message: '请求添加好友' })
  ElMessage.success('好友申请已发送')
}

async function loadRequests() {
  receivedRequests.value = await friendsApi.received()
  sentRequests.value = await friendsApi.sent()
}

async function acceptRequest(id) {
  await friendsApi.accept(id)
  await loadRequests()
  await reloadBadges()
}

async function rejectRequest(id) {
  await friendsApi.reject(id)
  await loadRequests()
}

async function createGroup() {
  const group = await groupsApi.create(newGroup)
  groups.value.unshift(group)
  subscribeGroup(group.id)
  groupDialog.value = false
  newGroup.name = ''
  newGroup.description = ''
}

async function joinGroup() {
  if (!joinGroupId.value) return
  await groupsApi.join(joinGroupId.value)
  joinGroupId.value = ''
  await reloadBadges()
}

async function leaveGroup() {
  await ElMessageBox.confirm('确定退出当前群聊？', '危险操作')
  await groupsApi.leave(activeTarget.value.id)
  activeTarget.value = null
  await reloadBadges()
}

async function removeMember(userId) {
  await ElMessageBox.confirm('确定移除该成员？', '危险操作')
  await groupsApi.remove(activeTarget.value.id, userId)
  members.value = await groupsApi.members(activeTarget.value.id)
}

async function saveProfile() {
  auth.user = await usersApi.update(profile)
  auth.persist()
  ElMessage.success('资料已保存')
}

async function changePassword() {
  await usersApi.password(password)
  password.oldPassword = ''
  password.newPassword = ''
  ElMessage.success('密码已修改')
}

async function readNotice(id) {
  await notificationsApi.read(id)
  await reloadBadges()
}

async function readAll() {
  await notificationsApi.readAll()
  await reloadBadges()
}

async function loadAdmin() {
  Object.assign(metrics, await adminApi.metrics())
  adminUsers.value = await adminApi.users()
}

async function ban(id) {
  await ElMessageBox.confirm('确定封禁该用户？', '管理员操作')
  await adminApi.ban(id)
  await loadAdmin()
}

async function unban(id) {
  await adminApi.unban(id)
  await loadAdmin()
}

async function handleLogout() {
  if (stomp.value) await stomp.value.deactivate()
  await auth.logout()
  router.push('/login')
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : ''
}
</script>
