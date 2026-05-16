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
- AI 聊天：答疑助手、总结助手、氛围助手，默认 Mock 模式，无需真实 API Key
- 1v1 语音聊天基础版：好友私聊发起、来电弹窗、接听、拒绝、挂断、WebRTC 音频信令

## 数据库
仍只使用 9 张核心表：
`user`、`friendship`、`friend_request`、`chat_group`、`group_member`、`message`、`file_record`、`notification`、`admin_log`。

AI 使用 `user` 表中的虚拟用户 `ai_qa/ai_summary/ai_mood` 作为消息发送方，回复进入现有 `message` 表。本轮未新增表，未修改表名。

## AI 配置
默认使用 Mock：
```bash
AI_PROVIDER=mock
```

真实 HTTP AI 可通过环境变量启用：
```bash
AI_PROVIDER=http
AI_API_BASE_URL=
AI_API_KEY=
AI_MODEL=
```

不要把真实 API Key 提交到代码或文档。HTTP 请求失败或缺少 Key 时会自动降级 Mock。

## 语音说明
语音通话当前是 1v1 基础版，只在好友私聊页面启用。浏览器或 Electron 需要授权麦克风；公网部署时 WebRTC NAT 穿透可能需要 STUN/TURN，本地课程演示优先使用同机多浏览器或局域网。

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
