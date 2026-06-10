<template>
  <section class="page-view">
    <header class="page-heading">
      <div><h1>群聊管理</h1><p>创建群聊、查看成员并使用用户 ID 管理成员。</p></div>
      <el-button type="primary" @click="createDialog = true">创建群聊</el-button>
    </header>
    <div class="content-grid two-column">
      <el-card shadow="never">
        <template #header><strong>我的群聊</strong></template>
        <button v-for="group in groups" :key="group.id" class="entity-row" @click="selectGroup(group)">
          <el-avatar>{{ group.name.slice(0, 1) }}</el-avatar>
          <span><strong>{{ group.name }}</strong><small>群 ID {{ group.id }} · {{ group.memberCount }} 人 · 群主 {{ group.ownerName }}</small></span>
        </button>
        <el-empty v-if="!groups.length" description="暂无群聊" />
      </el-card>
      <el-card shadow="never">
        <template #header><strong>群成员</strong></template>
        <div v-if="selected">
          <div class="detail-actions"><el-button type="primary" @click="openChat">进入群聊</el-button><el-button @click="leave">退出群聊</el-button></div>
          <div v-for="member in members" :key="member.userId" class="member-row">
            <span><strong>{{ member.nickname || member.username }}</strong><small>@{{ member.username }} · 用户 ID {{ member.userId }} · {{ member.role }}</small></span>
            <el-button v-if="selected.ownerId === auth.user?.id && member.role !== 'OWNER'" link type="danger" @click="remove(member.userId)">移除</el-button>
          </div>
        </div>
        <el-empty v-else description="选择群聊查看成员" />
      </el-card>
    </div>

    <el-dialog v-model="createDialog" title="创建群聊" width="520px">
      <el-form label-width="72px">
        <el-form-item label="群名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="邀请">
          <el-checkbox-group v-model="form.memberIds" class="friend-check-list">
            <el-checkbox v-for="friend in friends" :key="friend.id" :label="friend.id">
              {{ displayName(friend) }} · @{{ friend.username }} · ID {{ friend.id }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="createDialog = false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { friendsApi, groupsApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { displayName } from '../utils/display'

const router = useRouter()
const auth = useAuthStore()
const groups = ref([])
const friends = ref([])
const selected = ref(null)
const members = ref([])
const createDialog = ref(false)
const form = reactive({ name: '', description: '', memberIds: [] })

onMounted(load)
async function load() { groups.value = await groupsApi.list(); friends.value = await friendsApi.list() }
async function selectGroup(group) { selected.value = group; members.value = await groupsApi.members(group.id) }
async function create() { const group = await groupsApi.create(form); createDialog.value = false; Object.assign(form, { name: '', description: '', memberIds: [] }); await load(); await selectGroup(group); ElMessage.success('创建群聊成功') }
async function remove(userId) { await ElMessageBox.confirm(`确定移除用户 ID ${userId}？`, '移除成员'); await groupsApi.remove(selected.value.id, userId); members.value = await groupsApi.members(selected.value.id) }
async function leave() { await ElMessageBox.confirm('确定退出当前群聊？', '退出群聊'); await groupsApi.leave(selected.value.id); selected.value = null; members.value = []; await load() }
function openChat() { router.push({ path: '/app/chat', query: { type: 'GROUP', id: selected.value.id } }) }
</script>
