# OnlineChat 项目结构与开发接口文档

本文档基于当前源码整理，用于后续维护开发文档时快速了解项目目录、数据库表结构和已暴露接口。

## 1. 项目概览

OnlineChat 是一个 PC 端在线聊天平台，采用前后端分离架构：

- 后端：Java 17、Spring Boot 3.3.5、Spring Security、JWT、MyBatis-Plus、MySQL、Redis、STOMP WebSocket、Springdoc Swagger
- 前端：Vue 3、Vite、Electron、Element Plus、Pinia、Vue Router、Axios、STOMP/SockJS
- 数据库：MySQL 8.0，表结构由 `backend/src/main/resources/schema.sql` 初始化
- 文件存储：上传文件保存到后端工作目录下的 `uploads/`
- 接口返回：REST 接口统一返回 `ApiResult<T>`，结构为 `{ code, message, data }`，成功时 `code = 0`

后端启动时会自动创建演示账号：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | `ADMIN` |
| `alice` | `123456` | `USER` |
| `bob` | `123456` | `USER` |
| `carol` | `123456` | `USER` |

## 2. 当前项目结构

```text
onlinechat/
├── README.md
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── uploads/
│   └── src/
│       ├── main/
│       │   ├── java/com/whu/onlinechat/
│       │   │   ├── OnlineChatApplication.java
│       │   │   ├── common/          # 统一响应、业务异常、全局异常处理
│       │   │   ├── config/          # Security、WebSocket、OpenAPI 配置
│       │   │   ├── controller/      # REST Controller 与 STOMP 消息入口
│       │   │   ├── dto/             # 请求参数对象
│       │   │   ├── entity/          # MyBatis-Plus 实体
│       │   │   ├── mapper/          # MyBatis-Plus Mapper
│       │   │   ├── security/        # JWT、当前登录用户、鉴权过滤器
│       │   │   ├── service/         # 业务服务
│       │   │   ├── vo/              # 响应视图对象
│       │   │   └── websocket/       # WebSocket 用户与连接事件监听
│       │   └── resources/
│       │       ├── application.yml
│       │       └── schema.sql
│       └── test/
│           ├── java/
│           └── resources/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── electron/main.cjs
│   └── src/
│       ├── App.vue
│       ├── main.js
│       ├── styles.css
│       ├── api/             # Axios 实例与 REST API 封装
│       ├── router/          # Vue Router 与登录守卫
│       ├── stores/          # Pinia 鉴权状态
│       ├── views/           # 登录、注册、聊天主界面
│       └── websocket/       # STOMP 客户端创建
├── deploy/
│   ├── docker-compose.yml   # MySQL、Redis、后端容器
│   ├── mysql/
│   └── nginx/
└── docs/
    ├── API.md
    ├── DATABASE.md
    ├── DEMO_SCRIPT.md
    ├── RUN_GUIDE.md
    ├── TEST_GUIDE.md
    └── PROJECT_OVERVIEW.md
```

## 3. 后端模块说明

| 模块 | 说明 |
| --- | --- |
| `common` | `ApiResult` 统一响应，`BizException` 业务异常，`GlobalExceptionHandler` 异常转统一 JSON |
| `config` | HTTP 安全配置、CORS、STOMP 端点和消息代理、Swagger 配置 |
| `controller` | REST 接口和 WebSocket `@MessageMapping` 入口 |
| `dto` | 请求参数及校验注解 |
| `entity` | 数据库实体，与 MyBatis-Plus 表映射对应 |
| `mapper` | 基于 MyBatis-Plus 的数据访问接口 |
| `security` | JWT 生成解析、请求过滤器、当前用户对象 |
| `service` | 用户、好友、群聊、消息、文件、通知、后台等核心业务 |
| `vo` | REST 和 WebSocket 返回对象 |
| `websocket` | WebSocket 用户主体和在线/离线事件监听 |

## 4. 前端模块说明

| 模块 | 说明 |
| --- | --- |
| `src/api/http.js` | Axios 实例，默认 `baseURL=http://localhost:8080/api`，自动携带 `onlinechat_token` |
| `src/api/auth.js` | 登录、注册、退出、当前用户接口 |
| `src/api/modules.js` | 用户、好友、群聊、消息、文件、通知、管理员接口封装 |
| `src/websocket/client.js` | STOMP 客户端，默认连接 `http://localhost:8080/ws` |
| `src/router/index.js` | Hash 路由，包含登录守卫 |
| `src/stores/auth.js` | 登录态、token、当前用户管理 |
| `src/views` | `LoginView`、`RegisterView`、`ChatHomeView` |

## 5. 数据库表

### 5.1 `user`

用户表，逻辑删除字段为 `deleted`。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | 登录用户名 |
| `email` | VARCHAR(120) | NOT NULL, UNIQUE | 邮箱 |
| `password_hash` | VARCHAR(100) | NOT NULL | BCrypt 密码哈希 |
| `nickname` | VARCHAR(50) | NOT NULL | 昵称 |
| `avatar_url` | VARCHAR(500) | NULL | 头像 URL |
| `bio` | VARCHAR(500) | NULL | 个人简介 |
| `status` | VARCHAR(20) | NOT NULL, `OFFLINE` | 用户状态：`OFFLINE`、`ONLINE`、`BANNED` |
| `role` | VARCHAR(20) | NOT NULL, `USER` | 用户角色：`USER`、`ADMIN` |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | NOT NULL, `0` | 逻辑删除标记 |

### 5.2 `friendship`

好友关系表，双向好友会保存两条记录。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 关系 ID |
| `user_id` | BIGINT | NOT NULL | 当前用户 ID |
| `friend_id` | BIGINT | NOT NULL | 好友用户 ID |
| `remark` | VARCHAR(50) | NULL | 好友备注 |
| `status` | VARCHAR(20) | NOT NULL, `ACTIVE` | 关系状态：`ACTIVE`、`DELETED` |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | NOT NULL, `0` | 逻辑删除标记 |

索引：`uk_friendship_user_friend(user_id, friend_id)`。

### 5.3 `friend_request`

好友申请表。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 申请 ID |
| `from_user_id` | BIGINT | NOT NULL | 申请人 ID |
| `to_user_id` | BIGINT | NOT NULL | 接收人 ID |
| `status` | VARCHAR(20) | NOT NULL, `PENDING` | 状态：`PENDING`、`ACCEPTED`、`REJECTED` |
| `message` | VARCHAR(255) | NULL | 申请留言 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

索引：`idx_friend_request_from_to_status(from_user_id, to_user_id, status)`。

### 5.4 `chat_group`

群聊表，逻辑删除字段为 `deleted`。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 群 ID |
| `name` | VARCHAR(80) | NOT NULL | 群名 |
| `owner_id` | BIGINT | NOT NULL | 群主用户 ID |
| `avatar_url` | VARCHAR(500) | NULL | 群头像 |
| `description` | VARCHAR(500) | NULL | 群简介 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | NOT NULL, `0` | 逻辑删除标记 |

### 5.5 `group_member`

群成员表，逻辑删除字段为 `deleted`。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 成员关系 ID |
| `group_id` | BIGINT | NOT NULL | 群 ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `role` | VARCHAR(20) | NOT NULL, `MEMBER` | 群角色：`OWNER`、`MEMBER` |
| `joined_time` | DATETIME | CURRENT_TIMESTAMP | 入群时间 |
| `deleted` | TINYINT | NOT NULL, `0` | 逻辑删除标记 |

索引：`uk_group_member_group_user(group_id, user_id)`。

### 5.6 `message`

消息表，私聊和群聊共用，逻辑删除字段为 `deleted`。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 消息 ID |
| `conversation_type` | VARCHAR(20) | NOT NULL | 会话类型：`PRIVATE`、`GROUP` |
| `sender_id` | BIGINT | NOT NULL | 发送者 ID |
| `receiver_id` | BIGINT | NULL | 私聊接收者 ID |
| `group_id` | BIGINT | NULL | 群聊 ID |
| `content` | TEXT | NULL | 文本内容或文件说明 |
| `message_type` | VARCHAR(20) | NOT NULL, `TEXT` | 消息类型，默认 `TEXT`，文件消息可传业务自定义类型 |
| `file_id` | BIGINT | NULL | 关联 `file_record.id` |
| `recalled` | TINYINT | NOT NULL, `0` | 是否已撤回 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | NOT NULL, `0` | 逻辑删除标记 |

索引：`idx_message_sender(sender_id)`、`idx_message_receiver(receiver_id)`、`idx_message_group(group_id)`、`idx_message_create_time(create_time)`。

### 5.7 `file_record`

文件上传记录表。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 文件 ID |
| `uploader_id` | BIGINT | NOT NULL | 上传者 ID |
| `original_name` | VARCHAR(255) | NOT NULL | 原始文件名 |
| `stored_name` | VARCHAR(255) | NOT NULL | 存储文件名 |
| `file_path` | VARCHAR(500) | NOT NULL | 服务器本地存储路径 |
| `file_size` | BIGINT | NOT NULL | 文件大小，字节 |
| `mime_type` | VARCHAR(120) | NULL | MIME 类型 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 上传时间 |

单文件限制：20MB。

### 5.8 `notification`

通知表，用于好友申请、好友通过、私聊未读、群聊未读等。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 通知 ID |
| `receiver_id` | BIGINT | NOT NULL | 接收用户 ID |
| `type` | VARCHAR(50) | NOT NULL | 通知类型：`FRIEND_REQUEST`、`FRIEND_ACCEPTED`、`PRIVATE_MESSAGE`、`GROUP_MESSAGE` |
| `content` | VARCHAR(500) | NOT NULL | 通知内容 |
| `read_flag` | TINYINT | NOT NULL, `0` | 是否已读 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |

索引：`idx_notification_receiver_read(receiver_id, read_flag)`。

### 5.9 `admin_log`

管理员操作日志表。

| 字段 | 类型 | 约束/默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, AUTO_INCREMENT | 日志 ID |
| `admin_id` | BIGINT | NOT NULL | 管理员 ID |
| `operation_type` | VARCHAR(80) | NOT NULL | 操作类型：`BAN_USER`、`UNBAN_USER` |
| `target_user_id` | BIGINT | NULL | 被操作用户 ID |
| `content` | VARCHAR(500) | NULL | 操作内容 |
| `create_time` | DATETIME | CURRENT_TIMESTAMP | 创建时间 |

## 6. REST 接口约定

除注册、登录、Swagger、WebSocket 握手端点外，其余 HTTP 接口都需要请求头：

```http
Authorization: Bearer <jwt-token>
```

统一成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

统一错误响应：

```json
{
  "code": 1,
  "message": "错误原因",
  "data": null
}
```

### 6.1 认证接口 `/api/auth`

| 方法 | 路径 | 认证 | 请求体/参数 | 返回 |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | 否 | `RegisterRequest` | `UserVO` |
| POST | `/api/auth/login` | 否 | `LoginRequest` | `LoginVO` |
| POST | `/api/auth/logout` | 是 | 无 | `null` |
| GET | `/api/auth/me` | 是 | 无 | `UserVO` |

请求体：

- `RegisterRequest`: `username` 3-50，`email` 邮箱格式，`password` 6-50
- `LoginRequest`: `username`，`password`

返回：

- `LoginVO`: `token`，`user`
- `UserVO`: `id`，`username`，`email`，`nickname`，`avatarUrl`，`bio`，`status`，`role`

### 6.2 用户接口 `/api/users`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| GET | `/api/users/me` | 无 | `UserVO` |
| PUT | `/api/users/me` | `UpdateProfileRequest` | `UserVO` |
| PUT | `/api/users/me/password` | `ChangePasswordRequest` | `null` |
| GET | `/api/users/search?keyword=xxx` | `keyword` 可选 | `List<UserVO>`，最多 20 条 |

请求体：

- `UpdateProfileRequest`: `nickname` 最大 50，`avatarUrl` 最大 500，`bio` 最大 500
- `ChangePasswordRequest`: `oldPassword`，`newPassword` 6-50

### 6.3 好友接口 `/api/friends`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| GET | `/api/friends` | 无 | `List<FriendVO>` |
| POST | `/api/friends/requests` | `FriendRequestCreateRequest` | `null` |
| GET | `/api/friends/requests/received` | 无 | `List<FriendRequestVO>` |
| GET | `/api/friends/requests/sent` | 无 | `List<FriendRequestVO>` |
| POST | `/api/friends/requests/{id}/accept` | 路径参数 `id` | `null` |
| POST | `/api/friends/requests/{id}/reject` | 路径参数 `id` | `null` |
| PUT | `/api/friends/{friendId}/remark` | `RemarkRequest` | `null` |
| DELETE | `/api/friends/{friendId}` | 路径参数 `friendId` | `null` |

请求体：

- `FriendRequestCreateRequest`: `toUserId` 必填，`message` 可选
- `RemarkRequest`: `remark`

返回：

- `FriendVO`: `id`，`username`，`nickname`，`avatarUrl`，`bio`，`status`，`remark`，`unreadCount`
- `FriendRequestVO`: `id`，`fromUserId`，`fromUsername`，`fromNickname`，`toUserId`，`toUsername`，`toNickname`，`status`，`message`，`createTime`

### 6.4 群聊接口 `/api/groups`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| POST | `/api/groups` | `CreateGroupRequest` | `GroupVO` |
| GET | `/api/groups` | 无 | `List<GroupVO>` |
| GET | `/api/groups/{id}` | 路径参数 `id` | `GroupVO` |
| POST | `/api/groups/{id}/join` | 路径参数 `id` | `null` |
| POST | `/api/groups/{id}/leave` | 路径参数 `id` | `null` |
| GET | `/api/groups/{id}/members` | 路径参数 `id` | `List<GroupMemberVO>` |
| DELETE | `/api/groups/{id}/members/{userId}` | 群 ID 和用户 ID | `null` |

请求体：

- `CreateGroupRequest`: `name` 必填且最大 80，`avatarUrl`，`description` 最大 500，`memberIds`
- 创建群聊时只能邀请好友；群主角色为 `OWNER`，被邀请成员为 `MEMBER`
- 群主不能通过 `leave` 直接退出；移除成员仅群主可操作

返回：

- `GroupVO`: `id`，`name`，`ownerId`，`ownerName`，`avatarUrl`，`description`，`memberCount`，`unreadCount`，`createTime`
- `GroupMemberVO`: `userId`，`username`，`nickname`，`avatarUrl`，`status`，`role`，`joinedTime`

### 6.5 消息接口 `/api/messages`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| GET | `/api/messages/private/{friendId}` | `page` 默认 1，`size` 默认 20，`keyword` 可选 | `List<MessageVO>` |
| GET | `/api/messages/group/{groupId}` | `page` 默认 1，`size` 默认 20，`keyword` 可选 | `List<MessageVO>` |
| GET | `/api/messages/search?keyword=xxx` | `keyword` 可选 | `List<MessageVO>`，最多 50 条 |
| POST | `/api/messages/{messageId}/recall` | 路径参数 `messageId` | `MessageVO` |
| POST | `/api/messages/read/private/{friendId}` | 路径参数 `friendId` | `null` |
| POST | `/api/messages/read/group/{groupId}` | 路径参数 `groupId` | `null` |

说明：

- 私聊历史仅查询双方之间的 `PRIVATE` 消息。
- 群聊历史要求当前用户是群成员。
- 撤回仅允许发送者撤回 2 分钟内消息，撤回后内容更新为 `消息已撤回`。
- `read/private` 和 `read/group` 会将对应未读通知置为已读。

返回：

- `MessageVO`: `messageId`，`conversationType`，`senderId`，`senderName`，`receiverId`，`groupId`，`content`，`messageType`，`fileId`，`recalled`，`createTime`

### 6.6 文件接口 `/api/files`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| POST | `/api/files/upload` | `multipart/form-data`，字段名 `file` | `FileRecordVO` |
| GET | `/api/files/{id}/download` | 路径参数 `id` | 文件流 |

返回：

- `FileRecordVO`: `id`，`originalName`，`fileSize`，`mimeType`，`downloadUrl`，`createTime`

### 6.7 通知接口 `/api/notifications`

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| GET | `/api/notifications` | 无 | `List<NotificationVO>` |
| GET | `/api/notifications/unread-count` | 无 | `{ "count": number }` |
| POST | `/api/notifications/{id}/read` | 路径参数 `id` | `null` |
| POST | `/api/notifications/read-all` | 无 | `null` |

返回：

- `NotificationVO`: `id`，`type`，`content`，`read`，`createTime`

### 6.8 管理员接口 `/api/admin`

管理员接口要求 JWT 用户角色为 `ADMIN`。

| 方法 | 路径 | 请求体/参数 | 返回 |
| --- | --- | --- | --- |
| GET | `/api/admin/users` | 无 | `List<UserVO>` |
| POST | `/api/admin/users/{id}/ban` | 路径参数 `id` | `null` |
| POST | `/api/admin/users/{id}/unban` | 路径参数 `id` | `null` |
| GET | `/api/admin/metrics` | 无 | `AdminMetricsVO` |
| GET | `/api/admin/logs` | 无 | `List<AdminLog>`，最多 100 条 |

说明：

- 管理员不能封禁管理员账号。
- `ban` 会将用户状态改为 `BANNED`，`unban` 会将用户状态改为 `OFFLINE`。
- 封禁和解封会写入 `admin_log`。

返回：

- `AdminMetricsVO`: `onlineCount`，`todayMessageCount`，`todayNewUserCount`
- `AdminLog`: `id`，`adminId`，`operationType`，`targetUserId`，`content`，`createTime`

## 7. WebSocket / STOMP 接口

### 7.1 连接配置

后端端点：

- SockJS：`/ws`
- 原生 WebSocket：`/ws-native`

STOMP 配置：

- 客户端发送前缀：`/app`
- 服务端广播前缀：`/topic`
- 用户队列前缀：`/user`
- 简单 Broker：`/topic`、`/queue`

连接时需要 STOMP Header：

```http
Authorization: Bearer <jwt-token>
```

前端默认连接地址：`VITE_WS_URL`，未配置时为 `http://localhost:8080/ws`。

### 7.2 客户端发送目的地

| 目的地 | Payload | 说明 |
| --- | --- | --- |
| `/app/chat.private` | `ChatMessageRequest` | 发送私聊消息 |
| `/app/chat.group` | `ChatMessageRequest` | 发送群聊消息 |
| `/app/chat.recall` | `RecallMessageRequest` | 撤回消息 |
| `/app/read.private` | `ChatMessageRequest`，使用 `receiverId` | 标记私聊已读 |
| `/app/read.group` | `ChatMessageRequest`，使用 `groupId` | 标记群聊已读 |

Payload：

- `ChatMessageRequest`: `receiverId`，`groupId`，`content`，`messageType`，`fileId`
- `RecallMessageRequest`: `messageId`

### 7.3 客户端订阅目的地

| 目的地 | 数据 | 说明 |
| --- | --- | --- |
| `/user/queue/messages` | `MessageVO` | 私聊消息、私聊撤回消息 |
| `/topic/groups/{groupId}` | `MessageVO` | 指定群聊消息、群聊撤回消息 |
| `/user/queue/notifications` | `NotificationVO` | 当前用户通知 |
| `/topic/online` | 用户 ID | 用户上线/离线事件 |

## 8. 状态值与业务枚举

| 分类 | 当前值 |
| --- | --- |
| 用户状态 `user.status` | `OFFLINE`、`ONLINE`、`BANNED` |
| 用户角色 `user.role` | `USER`、`ADMIN` |
| 好友关系状态 `friendship.status` | `ACTIVE`、`DELETED` |
| 好友申请状态 `friend_request.status` | `PENDING`、`ACCEPTED`、`REJECTED` |
| 群成员角色 `group_member.role` | `OWNER`、`MEMBER` |
| 会话类型 `message.conversation_type` | `PRIVATE`、`GROUP` |
| 消息类型 `message.message_type` | 默认 `TEXT`，文件消息由前端/业务传入 |
| 通知类型 `notification.type` | `FRIEND_REQUEST`、`FRIEND_ACCEPTED`、`PRIVATE_MESSAGE`、`GROUP_MESSAGE` |
| 管理操作 `admin_log.operation_type` | `BAN_USER`、`UNBAN_USER` |

## 9. 运行与部署相关配置

后端关键配置位于 `backend/src/main/resources/application.yml`：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 后端端口 |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/onlinechat...` | MySQL 连接 |
| `SPRING_DATASOURCE_USERNAME` | `onlinechat` | MySQL 用户 |
| `SPRING_DATASOURCE_PASSWORD` | `change_me_app` | MySQL 密码 |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis 地址 |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis 端口 |
| `JWT_SECRET` | 开发默认密钥 | JWT 签名密钥，生产环境必须替换 |
| `JWT_EXPIRE_MINUTES` | `1440` | JWT 过期分钟数 |

前端关键环境变量：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | REST API 地址 |
| `VITE_WS_URL` | `http://localhost:8080/ws` | SockJS WebSocket 地址 |

Docker 编排位于 `deploy/docker-compose.yml`，包含 MySQL、Redis 和后端服务。

## 10. 需要注意的开发细节

- Spring Security 放行 `POST /api/auth/register`、`POST /api/auth/login`、Swagger 和 `/ws/**`，其余 REST 接口需要登录。
- WebSocket 握手端点虽然放行，但 STOMP `CONNECT` 阶段会校验 JWT，并拒绝不存在或已封禁用户。
- 在线状态由 WebSocket 连接事件维护，内存为主，Redis 为 best-effort 辅助记录。
- MyBatis-Plus 启用了 `deleted` 逻辑删除字段，涉及 `user`、`friendship`、`chat_group`、`group_member`、`message` 等实体。
- 消息搜索 `/api/messages/search` 当前只覆盖当前用户发送或接收的私聊消息条件；群聊消息搜索建议后续按群成员关系扩展。
- 上传文件仅记录本地路径并通过下载接口读取，当前未做文件归属权限细分。
