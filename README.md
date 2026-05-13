# OnlineChat

武汉大学软件工程课程实验项目：PC 端在线聊天平台 OnlineChat。

## 技术栈

- 后端：Java 17, Spring Boot 3, Spring Security, MyBatis-Plus, MySQL 8, Redis 7, JWT, Swagger
- 前端：Electron, Vue 3, Vite, Element Plus, Pinia, Vue Router, Axios, STOMP over WebSocket
- 部署：Docker Compose

## 当前阶段

已完成第 1-3 步的最小闭环：

- monorepo 项目结构
- Docker Compose 基础部署配置
- Spring Boot 后端基础配置
- 用户注册、登录、JWT 鉴权、BCrypt 密码加密
- Swagger 接口文档
- Vue/Electron 前端基础配置
- 登录页、注册页、主界面占位页
- 前端 token 持久化、Axios 自动携带 `Authorization: Bearer xxx`
- 路由守卫：未登录不能进入主界面

## 演示账号

首次启动后端会自动创建：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员 |
| alice | 123456 | 普通用户 |
| bob | 123456 | 普通用户 |
| carol | 123456 | 普通用户 |

## 快速运行

启动依赖：

```bash
cd onlinechat/deploy
docker compose up -d mysql redis
```

启动后端：

```bash
cd onlinechat/backend
mvn spring-boot:run
```

启动前端：

```bash
cd onlinechat/frontend
npm install
npm run dev
```

Electron 客户端：

```bash
cd onlinechat/frontend
npm run electron:dev
```

Swagger 地址：<http://localhost:8080/swagger-ui/index.html>

