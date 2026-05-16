# Run Guide

## 后端
```bash
cd /home/genevievelin/Online-chat/onlinechat/backend
mvn spring-boot:run
```

默认连接：
- MySQL: `jdbc:mysql://localhost:3306/onlinechat`
- Redis: `localhost:6379`
- Swagger: `http://localhost:8080/swagger-ui.html`

可通过环境变量覆盖 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_HOST`。

AI 默认 Mock 模式，无需 API Key：
```bash
AI_PROVIDER=mock mvn spring-boot:run
```

如需接入真实 HTTP AI：
```bash
AI_PROVIDER=http \
AI_API_BASE_URL= \
AI_API_KEY= \
AI_MODEL= \
mvn spring-boot:run
```

不要提交真实 API Key。HTTP Provider 期望远端返回 JSON 中的 `content` 或 `text` 字段；缺少 Key 或请求失败会自动降级 Mock。

## 前端
```bash
cd /home/genevievelin/Online-chat/onlinechat/frontend
npm run dev
```

浏览器访问 Vite 输出地址，默认 `http://localhost:5173`。Electron 演示可运行：

```bash
npm run electron:dev
```

语音通话需要浏览器或 Electron 麦克风权限。当前实现为 1v1 基础版，公网部署可能需要额外 STUN/TURN；课程本地演示建议同机多浏览器或局域网测试。

## 测试账号
- `alice / 123456`
- `bob / 123456`
- `carol / 123456`
- `admin / 123456`
