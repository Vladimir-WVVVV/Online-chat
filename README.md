# OnlineChat

武汉大学软件工程课程实验项目：PC 端在线聊天平台 OnlineChat。

## 技术栈
- 后端：Java 17、Spring Boot 3、Spring Security、MyBatis-Plus、MySQL、Redis、JWT、Swagger、STOMP WebSocket
- 前端：Vue 3、Electron、Vite、Element Plus、Pinia、Vue Router、Axios、STOMP

## 已完成功能
- 登录、注册、JWT 鉴权、BCrypt、路由守卫
- 用户资料、修改密码、封禁状态校验
- 好友搜索、申请、接受/拒绝、备注、删除、在线状态和未读角标
- STOMP 单聊、群聊、心跳、自动重连、多端消息推送、2 分钟内撤回
- 私聊/群聊历史分页、关键词搜索
- 图片和普通文件上传下载，20MB 限制，文件保存到 `backend/uploads/`
- 通知中心、单条已读、全部已读
- 管理员后台：用户列表、封禁/解封、在线人数、今日消息数、今日新增用户数、操作日志

## 数据库
仍只使用 9 张核心表：
`user`、`friendship`、`friend_request`、`chat_group`、`group_member`、`message`、`file_record`、`notification`、`admin_log`。

本轮未实现 AI、电话、语音连麦、WebRTC，也未新增相关表。

## 快速运行
```bash
cd deploy
docker compose up -d mysql redis

cd ../backend
mvn spring-boot:run

cd ../frontend
npm run dev
```

Swagger: <http://localhost:8080/swagger-ui.html>

测试账号：`alice / 123456`、`bob / 123456`、`carol / 123456`、`admin / 123456`。

更多说明见 `docs/RUN_GUIDE.md`、`docs/API.md`、`docs/DEMO_SCRIPT.md`。
