# Database

本轮仍只使用 9 张核心表，没有新增 AI、语音、WebRTC、已读回执表。

- `user`
- `friendship`
- `friend_request`
- `chat_group`
- `group_member`
- `message`
- `file_record`
- `notification`
- `admin_log`

未读简化规则：
- 好友申请、新私聊、新群聊都写入 `notification`。
- 打开会话后调用 read API，将对应通知标记已读。
- 消息撤回使用 `message.recalled` 和 `message.content='消息已撤回'`。

用户状态：
- `ONLINE` 由 WebSocket 在线会话在内存中辅助展示。
- `OFFLINE` 为默认离线状态。
- `BANNED` 用户不能登录，携带旧 token 继续请求也会被拒绝认证。

文件：
- 文件保存在 `backend/uploads/`。
- 元信息保存在 `file_record`。
