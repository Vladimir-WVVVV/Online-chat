<template>
  <main class="app-shell chat-shell" :class="{ 'with-info': activeTarget?.type === 'GROUP' }">
    <aside class="sidebar" :class="{ 'mobile-hidden': activeTarget }">
      <header class="panel-header">
        <div>
          <h2>会话</h2>
          <span>好友与群聊消息</span>
        </div>
      </header>

      <el-tabs v-model="leftTab" stretch class="left-tabs">
        <el-tab-pane label="好友" name="friends">
          <button
            v-for="friend in friends"
            :key="friend.id"
            class="list-item"
            :class="{ active: activeTarget?.type === 'PRIVATE' && activeTarget.id === friend.id }"
            @click="openPrivate(friend)"
          >
            <el-avatar :size="36" :src="assetUrl(friend.avatarUrl)">{{ displayInitial(friend) }}</el-avatar>
            <span>
              <strong>{{ friend.remark || friend.nickname || friend.username }}</strong>
              <small>@{{ friend.username }} · ID {{ friend.id }} · {{ friend.status }}</small>
            </span>
            <el-badge v-if="friend.unreadCount" :value="friend.unreadCount" />
          </button>
          <el-empty v-if="!friends.length" description="暂无好友" />
        </el-tab-pane>
        <el-tab-pane label="群聊" name="groups">
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
        <el-button class="mobile-back" size="small" @click="backToList">返回</el-button>
        <div>
          <h3>{{ activeTitle }}</h3>
          <span>{{ connected ? '实时连接已建立' : '正在等待连接' }}</span>
        </div>
        <div class="header-actions">
          <el-input v-model="keyword" size="small" placeholder="搜索当前历史" clearable @keyup.enter="loadHistory" />
          <el-button size="small" @click="loadHistory">搜索</el-button>
          <el-button v-if="activeTarget?.type === 'GROUP'" size="small" @click="rightPanelOpen = !rightPanelOpen">群成员</el-button>
        </div>
      </header>

      <div ref="messageListRef" class="message-list">
        <el-button v-if="activeTarget" size="small" text @click="loadMore">加载更早消息</el-button>
        <div v-if="!activeTarget" class="empty-state">选择好友或群聊开始课堂演示</div>
        <template v-for="item in displayMessages" :key="item.id">
          <div v-if="item.type === 'time'" class="time-separator">{{ item.text }}</div>
          <article
            v-else
            class="message-row"
            :class="{ mine: isSelfMessage(item.data), ai: isAiMessage(item.data) }"
          >
            <el-avatar class="message-avatar" :size="34" :src="assetUrl(item.data.senderAvatarUrl)">
              {{ String(item.data.senderName || '?').slice(0, 1) }}
            </el-avatar>
            <div class="bubble">
              <div class="message-meta">{{ item.data.senderName }} · {{ formatTime(item.data.createTime) }}</div>
              <template v-if="item.data.recalled">
                <em>消息已撤回</em>
              </template>
              <template v-else-if="item.data.messageType === 'IMAGE'">
                <img v-if="filePreviews[item.data.fileId]" class="chat-image" :src="filePreviews[item.data.fileId]" alt="图片消息" />
                <div>{{ item.data.content }}</div>
                <el-button size="small" @click="downloadFile(item.data.fileId)">下载</el-button>
              </template>
              <template v-else-if="item.data.messageType === 'FILE'">
                <div class="file-message">{{ item.data.content }}</div>
                <el-button size="small" @click="downloadFile(item.data.fileId)">下载文件</el-button>
              </template>
              <template v-else>{{ item.data.content }}</template>
              <el-button
                v-if="canRecall(item.data)"
                class="recall-button"
                size="small"
                link
                @click="recall(item.data)"
              >
                撤回
              </el-button>
            </div>
          </article>
        </template>
      </div>

      <footer class="composer">
        <div class="composer-tools">
          <el-upload :show-file-list="false" :before-upload="uploadAndSend">
            <el-button>文件</el-button>
          </el-upload>
          <el-button :disabled="!canStartVoice" :title="voiceSupport.message" @click="startVoiceCall">语音</el-button>
          <el-button :disabled="!activeTarget || aiLoading" @click="prepareAiQuestion">AI答疑</el-button>
          <el-button :disabled="!activeTarget || aiLoading" @click="callAi('SUMMARY')">总结</el-button>
          <el-button :disabled="!activeTarget || aiLoading" @click="callAi('MOOD')">氛围助手</el-button>
        </div>
        <el-input v-model="draft" type="textarea" :rows="3" :disabled="!activeTarget" placeholder="输入消息，Enter 发送" @keydown.enter.exact.prevent="sendText" />
        <el-button type="primary" :disabled="!activeTarget || !draft.trim()" @click="sendText">发送</el-button>
      </footer>
    </section>

    <aside v-if="activeTarget?.type === 'GROUP'" class="info-panel" :class="{ open: rightPanelOpen }">
      <el-button class="info-close" text @click="rightPanelOpen = false">关闭</el-button>
      <header class="side-panel-title"><h3>群成员</h3><small>群 ID {{ activeTarget.id }}</small></header>
      <div class="side-section">
        <div v-for="member in members" :key="member.userId" class="member-row">
          <span><strong>{{ member.nickname || member.username }}</strong><small>@{{ member.username }} · ID {{ member.userId }} · {{ member.role }}</small></span>
          <el-button v-if="activeGroupOwner && member.role !== 'OWNER'" size="small" link @click="removeMember(member.userId)">移除</el-button>
        </div>
        <el-button size="small" @click="leaveGroup">退出群聊</el-button>
      </div>
    </aside>

    <el-dialog v-model="incomingCall.visible" title="语音来电" width="360px" :close-on-click-modal="false">
      <p>{{ incomingCall.fromUsername || '好友' }} 邀请你语音通话</p>
      <template #footer>
        <el-button @click="rejectIncomingCall">拒绝</el-button>
        <el-button type="primary" @click="acceptIncomingCall">接听</el-button>
      </template>
    </el-dialog>

    <div v-if="voice.visible" class="voice-floating">
      <strong>{{ voice.peerName || '语音通话' }}</strong>
      <span>{{ voice.status }}</span>
      <el-button size="small" type="danger" @click="hangupVoice">挂断</el-button>
    </div>
  </main>
</template>

<script setup>
import axios from 'axios'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi, filesApi, friendsApi, groupsApi, messagesApi, notificationsApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { assetUrl, displayInitial } from '../utils/display'
import { createVoiceService, getVoiceSupport } from '../voice/voiceService'
import { createStompClient } from '../websocket/client'

const route = useRoute()
const auth = useAuthStore()
const leftTab = ref('friends')
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
const messageListRef = ref(null)
const filePreviews = reactive({})
const aiLoading = ref(false)
const pendingAiQuestion = ref(null)
const rightPanelOpen = ref(false)
const voiceSupport = getVoiceSupport()
const voice = reactive({ visible: false, status: '已挂断', peerId: null, peerName: '' })
const incomingCall = reactive({ visible: false, fromUserId: null, fromUsername: '' })
let voiceService = null
const TIME_GAP = 5 * 60 * 1000

const activeTitle = computed(() => activeTarget.value ? activeTarget.value.name : '聊天主界面')
const canStartVoice = computed(() => voiceSupport.supported && activeTarget.value?.type === 'PRIVATE' && stomp.value?.connected)
const orderedMessages = computed(() => sortMessages(messages.value))
const displayMessages = computed(() => {
  const result = []
  const sorted = orderedMessages.value
  for (let i = 0; i < sorted.length; i += 1) {
    const message = sorted[i]
    const currentTime = normalizeMessageTime(message)
    const previous = sorted[i - 1]
    const previousTime = previous ? normalizeMessageTime(previous) : 0
    if (i === 0 || currentTime - previousTime > TIME_GAP) {
      result.push({
        type: 'time',
        id: `time-${getMessageKey(message)}`,
        text: formatMessageTime(currentTime)
      })
    }
    result.push({
      type: 'message',
      id: `message-${getMessageKey(message)}`,
      data: message
    })
  }
  return result
})
const activeGroupOwner = computed(() => activeTarget.value?.type === 'GROUP' && activeTarget.value.ownerId === auth.user?.id)

onMounted(async () => {
  await reloadAll()
  await openFromRoute()
  connectSocket()
})

watch(() => [route.query.type, route.query.id], openFromRoute)

onUnmounted(() => {
  if (stomp.value) stomp.value.deactivate()
  voiceService?.cleanup()
})

async function reloadAll() {
  friends.value = await friendsApi.list()
  groups.value = await groupsApi.list()
  notifications.value = await notificationsApi.list()
}

async function openFromRoute() {
  const id = Number(route.query.id)
  if (!id) return
  if (route.query.type === 'PRIVATE') {
    const friend = friends.value.find((item) => item.id === id)
    if (friend) await openPrivate(friend)
  } else if (route.query.type === 'GROUP') {
    const group = groups.value.find((item) => item.id === id)
    if (group) await openGroup(group)
  }
}

function connectSocket() {
  stomp.value = createStompClient(auth.token, {
    onConnect: () => {
      connected.value = true
      stomp.value.subscribe('/user/queue/messages', (frame) => handleIncoming(JSON.parse(frame.body)))
      stomp.value.subscribe('/user/queue/voice', (frame) => voiceService?.handleSignal(JSON.parse(frame.body)))
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
  voiceService = createVoiceService({
    getStomp: () => stomp.value,
    getCurrentUser: () => auth.user,
    onSignal: handleVoiceSignal,
    onStatus: updateVoiceStatus
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
  if (isForActive(message)) {
    if (replacePendingAiQuestion(message)) return
    messages.value = mergeMessages(messages.value, [message])
  }
  scrollToBottom()
}

function isForActive(message) {
  if (!activeTarget.value) return false
  if (activeTarget.value.type === 'GROUP') return message.groupId === activeTarget.value.id
  return message.conversationType === 'PRIVATE'
    && (message.senderId === activeTarget.value.id
      || message.receiverId === activeTarget.value.id
      || (isAiMessage(message)
        && ((message.receiverId === auth.user?.id && message.groupId === activeTarget.value.id)
          || (message.receiverId === activeTarget.value.id && message.groupId === auth.user?.id))))
}

async function reloadBadges() {
  friends.value = await friendsApi.list()
  groups.value = await groupsApi.list()
  notifications.value = await notificationsApi.list()
}

async function openPrivate(friend) {
  activeTarget.value = { type: 'PRIVATE', id: friend.id, name: friend.remark || friend.nickname || friend.username }
  page.value = 1
  await loadHistory()
  await messagesApi.readPrivate(friend.id)
  await reloadBadges()
}

async function openGroup(group) {
  activeTarget.value = { type: 'GROUP', id: group.id, name: group.name, ownerId: group.ownerId }
  page.value = 1
  await loadHistory()
  members.value = await groupsApi.members(group.id)
  await messagesApi.readGroup(group.id)
  subscribeGroup(group.id)
  await reloadBadges()
}

async function loadHistory() {
  if (!activeTarget.value) return
  const shouldScrollToBottom = page.value === 1
  const params = { page: page.value, size: 20, keyword: keyword.value }
  const data = activeTarget.value.type === 'PRIVATE'
    ? await messagesApi.privateHistory(activeTarget.value.id, params)
    : await messagesApi.groupHistory(activeTarget.value.id, params)
  messages.value = mergeMessages(page.value === 1 ? [] : messages.value, data)
  for (const message of messages.value) await ensurePreview(message)
  if (shouldScrollToBottom) scrollToBottom()
}

async function loadMore() {
  page.value += 1
  await loadHistory()
}

async function sendText() {
  if (!draft.value.trim() || !activeTarget.value || !stomp.value?.connected) return
  if (await tryAiCommand(draft.value.trim())) {
    draft.value = ''
    return
  }
  const payload = { content: draft.value.trim(), messageType: 'TEXT' }
  if (activeTarget.value.type === 'PRIVATE') {
    stomp.value.publish({ destination: '/app/chat.private', body: JSON.stringify({ ...payload, receiverId: activeTarget.value.id }) })
  } else {
    stomp.value.publish({ destination: '/app/chat.group', body: JSON.stringify({ ...payload, groupId: activeTarget.value.id }) })
  }
  draft.value = ''
  scrollToBottom()
}

async function tryAiCommand(text) {
  if (text.startsWith('@AI ') || text.startsWith('@答疑助手 ')) {
    const content = text.replace(/^@(AI|答疑助手)\s+/, '').trim()
    await callAi('QA', content)
    return true
  }
  if (text === '/summary') {
    await callAi('SUMMARY')
    return true
  }
  if (text === '/mood') {
    await callAi('MOOD')
    return true
  }
  return false
}

function prepareAiQuestion() {
  if (!activeTarget.value) return
  if (!draft.value.trim()) {
    draft.value = '@AI '
    return
  }
  callAi('QA', draft.value.trim())
  draft.value = ''
}

async function callAi(agentType, content = '') {
  if (!activeTarget.value || aiLoading.value) return
  if (agentType === 'QA' && !content.trim()) {
    ElMessage.warning('请输入要提问的内容')
    return
  }
  aiLoading.value = true
  const questionTime = Date.now()
  const questionMessage = agentType === 'QA' ? createLocalAiQuestion(content, questionTime) : null
  if (questionMessage) {
    pendingAiQuestion.value = {
      tempId: questionMessage.tempId,
      content: questionMessage.content,
      conversationType: questionMessage.conversationType,
      targetId: activeTarget.value.id,
      startedAt: questionTime
    }
    messages.value = mergeMessages(messages.value, [questionMessage])
    scrollToBottom()
  }
  const loadingMessage = {
    tempId: `ai-loading-${Date.now()}`,
    conversationType: activeTarget.value.type,
    senderId: -1,
    senderName: 'AI 正在思考中...',
    receiverId: activeTarget.value.type === 'PRIVATE' ? auth.user?.id : null,
    groupId: activeTarget.value.id,
    content: 'AI 正在思考中...',
    messageType: 'TEXT',
    createTime: new Date(questionTime + 1).toISOString(),
    aiLoading: true
  }
  messages.value = mergeMessages(messages.value, [loadingMessage])
  scrollToBottom()
  try {
    const payload = {
      conversationType: activeTarget.value.type,
      targetId: activeTarget.value.id,
      agentType,
      content
    }
    const response = await (agentType === 'SUMMARY'
      ? aiApi.summary(payload)
      : agentType === 'MOOD'
        ? aiApi.mood(payload)
        : aiApi.chat(payload))
    const returnedMessages = extractAiMessages(response)
    const messagesToMerge = []
    for (const message of returnedMessages) {
      if (!replacePendingAiQuestion(message)) {
        messagesToMerge.push(message)
      }
    }
    if (messagesToMerge.length) {
      messages.value = mergeMessages(messages.value, messagesToMerge)
    }
  } catch (error) {
    ElMessage.error(error.message || 'AI 调用失败')
    if (agentType === 'QA') {
      messages.value = mergeMessages(messages.value, [createAiFailureMessage()])
    }
  } finally {
    messages.value = messages.value.filter((message) => message.tempId !== loadingMessage.tempId)
    pendingAiQuestion.value = null
    aiLoading.value = false
    scrollToBottom()
  }
}

function isAiMessage(message) {
  return ['答疑助手', '总结助手', '氛围助手'].includes(message?.senderName) || message?.aiLoading || message?.senderId === -1
}

function createLocalAiQuestion(content, createdAt = Date.now()) {
  return {
    tempId: `ai-question-${Date.now()}`,
    conversationType: activeTarget.value.type,
    senderId: auth.user?.id,
    senderName: auth.user?.nickname || auth.user?.username,
    senderAvatarUrl: auth.user?.avatarUrl,
    receiverId: activeTarget.value.type === 'PRIVATE' ? activeTarget.value.id : null,
    groupId: activeTarget.value.type === 'GROUP' ? activeTarget.value.id : null,
    content,
    messageType: 'TEXT',
    createTime: new Date(createdAt).toISOString(),
    aiQuestionPending: true
  }
}

function createAiFailureMessage() {
  return {
    tempId: `ai-error-${Date.now()}`,
    conversationType: activeTarget.value.type,
    senderId: -1,
    senderName: '答疑助手',
    receiverId: activeTarget.value.type === 'PRIVATE' ? auth.user?.id : null,
    groupId: activeTarget.value.id,
    content: 'AI答疑失败，请稍后重试',
    messageType: 'TEXT',
    createTime: new Date().toISOString()
  }
}

function extractAiMessages(response) {
  if (Array.isArray(response?.messages)) return response.messages
  return response?.message ? [response.message] : []
}

function replacePendingAiQuestion(message) {
  if (!isPendingAiQuestionMessage(message)) return false
  const pending = pendingAiQuestion.value
  const displayMessage = {
    ...message,
    createTime: new Date(pending.startedAt).toISOString()
  }
  messages.value = mergeMessages(
    messages.value.filter((item) => item.tempId !== pending.tempId),
    [displayMessage]
  )
  scrollToBottom()
  return true
}

function isPendingAiQuestionMessage(message) {
  const pending = pendingAiQuestion.value
  if (!pending || !hasPersistedMessageId(message) || !isSelfMessage(message)) return false
  if (message?.content !== pending.content) return false
  if (message?.conversationType !== pending.conversationType) return false
  if (!isSamePendingConversation(message, pending)) return false
  return Math.abs(normalizeMessageTime(message) - pending.startedAt) < 10 * 60 * 1000
}

function hasPersistedMessageId(message) {
  return getMessageId(message) > 0
}

function isSamePendingConversation(message, pending) {
  if (pending.conversationType === 'GROUP') {
    return normalizeId(message?.groupId) === normalizeId(pending.targetId)
  }
  return normalizeId(message?.receiverId) === normalizeId(pending.targetId)
}

async function startVoiceCall() {
  if (!canStartVoice.value) {
    ElMessage.warning(voiceSupport.message || '语音通话仅支持已连接的好友私聊')
    return
  }
  try {
    voice.visible = true
    voice.peerId = activeTarget.value.id
    voice.peerName = activeTarget.value.name
    voice.status = '呼叫中'
    await voiceService.startCall(activeTarget.value.id)
  } catch (error) {
    updateVoiceStatus(error.message || '连接失败')
    ElMessage.error(error.message || '语音通话启动失败')
  }
}

function handleVoiceSignal(signal) {
  incomingCall.visible = true
  incomingCall.fromUserId = signal.fromUserId
  incomingCall.fromUsername = signal.fromUsername
  voice.visible = true
  voice.peerId = signal.fromUserId
  voice.peerName = signal.fromUsername
  voice.status = '来电中'
}

async function acceptIncomingCall() {
  try {
    incomingCall.visible = false
    await voiceService.acceptCall(incomingCall.fromUserId)
    updateVoiceStatus('通话中')
  } catch (error) {
    updateVoiceStatus(error.message || '连接失败')
    ElMessage.error(error.message || '接听失败')
  }
}

function rejectIncomingCall() {
  voiceService.rejectCall(incomingCall.fromUserId)
  incomingCall.visible = false
  updateVoiceStatus('已挂断')
}

function hangupVoice() {
  try {
    voiceService.hangup(voice.peerId)
  } catch (error) {
    voiceService.cleanup()
  }
  updateVoiceStatus('已挂断')
}

function updateVoiceStatus(status) {
  voice.status = status
  if (['已挂断', '对方拒绝', '对方不在线或连接失败', '连接失败', '已拒绝'].includes(status)) {
    setTimeout(() => {
      if (voice.status === status) voice.visible = false
    }, 1800)
  }
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
  scrollToBottom()
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
  return isSelfMessage(message) && !message.recalled && Date.now() - normalizeMessageTime(message) < 120000
}

function normalizeId(id) {
  return id === null || id === undefined ? '' : String(id)
}

function getSenderId(message) {
  return message?.senderId ?? message?.sender?.id ?? message?.fromUserId
}

function isSelfMessage(message) {
  const senderId = normalizeId(getSenderId(message))
  const currentUserId = normalizeId(auth.user?.id)
  return Boolean(senderId && currentUserId && senderId === currentUserId)
}

async function recall(message) {
  const recalled = await messagesApi.recall(message.messageId)
  messages.value = mergeMessages(messages.value, [recalled])
  scrollToBottom()
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

function backToList() {
  activeTarget.value = null
  messages.value = []
  rightPanelOpen.value = false
}

function formatTime(value) {
  const time = normalizeMessageTime({ createTime: value })
  return time ? new Date(time).toLocaleString() : ''
}

function formatMessageTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const startOfMessageDay = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime()
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const clock = `${date.getHours()}:${minutes}`
  if (startOfMessageDay === startOfToday) return `今天 ${clock}`
  if (startOfMessageDay === startOfToday - 24 * 60 * 60 * 1000) return `昨天 ${clock}`
  if (date.getFullYear() === now.getFullYear()) return `${date.getMonth() + 1}月${date.getDate()}日 ${clock}`
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${clock}`
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

function normalizeMessageTime(message) {
  const raw = message?.createTime || message?.createdAt || message?.time
  if (!raw) return Date.now()
  if (typeof raw === 'number') return raw
  const normalized = String(raw).includes('T') ? String(raw) : String(raw).replace(' ', 'T')
  const time = new Date(normalized).getTime()
  return Number.isNaN(time) ? Date.now() : time
}

function getMessageId(message) {
  const id = Number(message?.messageId || message?.id || 0)
  return Number.isNaN(id) ? 0 : id
}

function normalizeMessage(message) {
  const nowIso = new Date().toISOString()
  const id = message?.messageId || message?.id || message?.tempId || message?.clientTempId
  return {
    ...message,
    messageId: id || `temp-${Date.now()}-${Math.random()}`,
    createTime: message?.createTime || message?.createdAt || message?.time || nowIso
  }
}

function getMessageKey(message) {
  return String(
    message?.messageId ||
    message?.id ||
    message?.tempId ||
    message?.clientTempId ||
    `${message?.senderId || 'unknown'}-${message?.receiverId || message?.groupId || 'target'}-${message?.createTime || ''}-${message?.content || ''}`
  )
}

function sortMessages(list) {
  return [...list].sort((a, b) => {
    const ta = normalizeMessageTime(a)
    const tb = normalizeMessageTime(b)
    if (ta !== tb) return ta - tb
    return getMessageId(a) - getMessageId(b)
  })
}

function mergeMessages(oldList, newList) {
  const map = new Map()
  for (const raw of [...oldList, ...newList]) {
    const message = normalizeMessage(raw)
    map.set(getMessageKey(message), message)
  }
  return sortMessages([...map.values()])
}
</script>
