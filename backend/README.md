# OnlineChat Backend

Spring Boot 3 后端，提供 REST API、JWT 安全认证、MyBatis-Plus 持久化、文件上传下载和 STOMP WebSocket。

## 启动
```bash
mvn spring-boot:run
```

默认需要 MySQL 和 Redis，配置见 `src/main/resources/application.yml`。测试环境使用 H2：

```bash
mvn test
```

## 主要模块
- `controller`：REST 和 WebSocket 入口
- `service`：用户、好友、群聊、消息、文件、通知、管理员业务
- `mapper/entity`：9 张核心表映射
- `security/config`：JWT、安全配置、Swagger、WebSocket

文件上传目录：`backend/uploads/`。
