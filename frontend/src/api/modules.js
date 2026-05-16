import http from './http'

export const usersApi = {
  me: () => http.get('/users/me'),
  update: (payload) => http.put('/users/me', payload),
  password: (payload) => http.put('/users/me/password', payload),
  search: (keyword) => http.get('/users/search', { params: { keyword } })
}

export const friendsApi = {
  list: () => http.get('/friends'),
  request: (payload) => http.post('/friends/requests', payload),
  received: () => http.get('/friends/requests/received'),
  sent: () => http.get('/friends/requests/sent'),
  accept: (id) => http.post(`/friends/requests/${id}/accept`),
  reject: (id) => http.post(`/friends/requests/${id}/reject`),
  remark: (friendId, remark) => http.put(`/friends/${friendId}/remark`, { remark }),
  delete: (friendId) => http.delete(`/friends/${friendId}`)
}

export const groupsApi = {
  create: (payload) => http.post('/groups', payload),
  list: () => http.get('/groups'),
  detail: (id) => http.get(`/groups/${id}`),
  join: (id) => http.post(`/groups/${id}/join`),
  leave: (id) => http.post(`/groups/${id}/leave`),
  members: (id) => http.get(`/groups/${id}/members`),
  remove: (groupId, userId) => http.delete(`/groups/${groupId}/members/${userId}`)
}

export const messagesApi = {
  privateHistory: (friendId, params) => http.get(`/messages/private/${friendId}`, { params }),
  groupHistory: (groupId, params) => http.get(`/messages/group/${groupId}`, { params }),
  search: (keyword) => http.get('/messages/search', { params: { keyword } }),
  recall: (id) => http.post(`/messages/${id}/recall`),
  readPrivate: (friendId) => http.post(`/messages/read/private/${friendId}`),
  readGroup: (groupId) => http.post(`/messages/read/group/${groupId}`)
}

export const aiApi = {
  chat: (payload) => http.post('/ai/chat', payload),
  summary: (payload) => http.post('/ai/summary', payload),
  mood: (payload) => http.post('/ai/mood', payload)
}

export const filesApi = {
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/files/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  downloadUrl: (id) => `${http.defaults.baseURL}/files/${id}/download`
}

export const notificationsApi = {
  list: () => http.get('/notifications'),
  unread: () => http.get('/notifications/unread-count'),
  read: (id) => http.post(`/notifications/${id}/read`),
  readAll: () => http.post('/notifications/read-all')
}

export const adminApi = {
  users: () => http.get('/admin/users'),
  ban: (id) => http.post(`/admin/users/${id}/ban`),
  unban: (id) => http.post(`/admin/users/${id}/unban`),
  metrics: () => http.get('/admin/metrics'),
  logs: () => http.get('/admin/logs')
}
