# Database

当前 `schema.sql` 对齐计划书《4.2 数据库表设计概览》，核心表共 9 张：

- `user`
- `friendship`
- `friend_request`
- `chat_group`
- `group_member`
- `message`
- `file_record`
- `notification`
- `admin_log`

MVP 阶段暂不创建 `message_read_receipts` 和 `ai_agents`。已读未读先通过 `notification.read_flag` 或消息相关字段简化处理；AI 功能后续通过代码中的 Mock Provider 实现，不依赖数据库表。

为避免 MySQL 关键字或系统表概念冲突，`schema.sql` 中使用反引号包裹表名，例如 `user`、`message`。

当前 schema 暂不添加物理外键，引用关系通过业务逻辑维护。已添加的唯一约束和索引包括：

- `user.username` 唯一
- `user.email` 唯一
- `friendship(user_id, friend_id)` 唯一
- `friend_request(from_user_id, to_user_id, status)`
- `group_member(group_id, user_id)` 唯一
- `message(sender_id)`
- `message(receiver_id)`
- `message(group_id)`
- `message(create_time)`
- `notification(receiver_id, read_flag)`
