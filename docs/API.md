# OnlineChat API

所有业务接口通过 `Authorization: Bearer <JWT>` 识别当前用户，前端不传 `currentUserId`。

## 用户
- `GET /api/users/me` 当前用户资料
- `PUT /api/users/me` 修改 `nickname/avatarUrl/bio`
- `PUT /api/users/me/password` 修改密码
- `GET /api/users/search?keyword=xxx` 搜索用户
- `POST /api/auth/logout` 注销本地登录态

## 好友
- `GET /api/friends`
- `POST /api/friends/requests`
- `GET /api/friends/requests/received`
- `GET /api/friends/requests/sent`
- `POST /api/friends/requests/{id}/accept`
- `POST /api/friends/requests/{id}/reject`
- `PUT /api/friends/{friendId}/remark`
- `DELETE /api/friends/{friendId}`

## 群聊
- `POST /api/groups` 创建群聊，body: `{ "name": "课程项目讨论群", "description": "软件工程课程项目群", "memberIds": [2, 3] }`；`memberIds` 可选，只能邀请好友
- `GET /api/groups`
- `GET /api/groups/{id}`
- `POST /api/groups/{id}/join`
- `POST /api/groups/{id}/leave`
- `GET /api/groups/{id}/members`
- `DELETE /api/groups/{id}/members/{userId}`

## 消息
- `GET /api/messages/private/{friendId}?page=1&size=20&keyword=`
- `GET /api/messages/group/{groupId}?page=1&size=20&keyword=`
- `GET /api/messages/search?keyword=`
- `POST /api/messages/{messageId}/recall`
- `POST /api/messages/read/private/{friendId}`
- `POST /api/messages/read/group/{groupId}`

消息返回字段包含 `messageId/conversationType/senderId/senderName/receiverId/groupId/content/messageType/fileId/recalled/createTime`。

## 文件
- `POST /api/files/upload`，multipart 字段名 `file`，单文件 20MB
- `GET /api/files/{id}/download`

## AI
所有 AI 接口需要 JWT，封禁用户不可调用。AI 默认 Mock 模式；回复保存为普通 `message`，`messageType=TEXT`，`senderName` 为 `答疑助手/总结助手/氛围助手`。

- `POST /api/ai/chat`
  body: `{ "conversationType": "PRIVATE", "targetId": 2, "agentType": "QA", "content": "帮我解释 WebSocket" }`
- `POST /api/ai/summary`
  body: `{ "conversationType": "GROUP", "targetId": 1 }`
- `POST /api/ai/mood`
  body: `{ "conversationType": "PRIVATE", "targetId": 2 }`

AI Provider 环境变量：
- `AI_PROVIDER=mock/http`
- `AI_API_BASE_URL=`
- `AI_API_KEY=`
- `AI_MODEL=`

不要提交真实 API Key。`AI_PROVIDER=http` 且 Key 或请求不可用时自动降级 Mock。

## 通知
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{id}/read`
- `POST /api/notifications/read-all`

## 管理员
- `GET /api/admin/users`
- `POST /api/admin/users/{id}/ban`
- `POST /api/admin/users/{id}/unban`
- `GET /api/admin/metrics`
- `GET /api/admin/logs`

## WebSocket/STOMP
连接端点：`/ws`，CONNECT header 传 `Authorization: Bearer <JWT>`。

客户端发送：
- `/app/chat.private`
- `/app/chat.group`
- `/app/chat.recall`
- `/app/read.private`
- `/app/read.group`
- `/app/voice.call`
- `/app/voice.accept`
- `/app/voice.reject`
- `/app/voice.offer`
- `/app/voice.answer`
- `/app/voice.ice`
- `/app/voice.hangup`

客户端订阅：
- `/user/queue/messages`
- `/user/queue/notifications`
- `/user/queue/voice`
- `/topic/groups/{groupId}`
- `/topic/online`

语音信令结构：
```json
{
  "type": "CALL",
  "fromUserId": 1,
  "toUserId": 2,
  "fromUsername": "alice",
  "targetUserId": 2,
  "sdp": "...",
  "candidate": {},
  "timestamp": "2026-05-16T10:00:00"
}
```

语音信令只转发不入库；只能给好友发送，封禁用户不能发起或响应，对方离线时通过 `/user/queue/voice` 返回 `ERROR`。
