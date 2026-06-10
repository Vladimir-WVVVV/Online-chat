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

`mvn spring-boot:run` 不会自动读取仓库根目录 `.env`。如需接入真实 HTTP AI，可直接在命令中传入环境变量：
```bash
AI_PROVIDER=http \
AI_API_BASE_URL=https://open.bigmodel.cn/api/paas/v4/chat/completions \
AI_API_KEY='你的本地 API Key' \
AI_MODEL=glm-4.5-flash \
mvn spring-boot:run
```

也可以复制根目录示例配置并使用本地启动脚本。脚本会加载根目录 `.env`，并将 `AI_PROVIDER` 强制设为 `http`：

```bash
cd /home/genevievelin/Online-chat/onlinechat
cp .env.example .env
# 编辑 .env，只填写本机的 AI_API_KEY
./backend/run-ai-local.sh
```

Docker Compose 从 `deploy/` 目录启动时也不会自动读取仓库根目录 `.env`，需要显式指定：

```bash
cd /home/genevievelin/Online-chat/onlinechat/deploy
docker compose --env-file ../.env up -d --build backend
```

使用 Docker Compose 前确认根目录 `.env` 中为 `AI_PROVIDER=http`，并已填写 `AI_API_KEY`。

实际读取关系：

| 用途 | Spring 配置项 | 环境变量 |
| --- | --- | --- |
| Provider | `ai.provider` | `AI_PROVIDER` |
| 模型 | `ai.model` | `AI_MODEL` |
| HTTP 地址 | `ai.api-base-url` | `AI_API_BASE_URL` |
| API Key | `ai.api-key` | `AI_API_KEY` |

当前只有 `application.yml`，不存在 `application-dev.yml` 或 `application-local.yml`。不要提交真实 API Key。缺少 Key、地址或 HTTP 请求失败时会自动降级 Mock。

## 前端
```bash
cd /home/genevievelin/Online-chat/onlinechat/frontend
npm run dev
```

浏览器访问 Vite 输出地址，默认 `http://localhost:5173`。Electron 演示可运行：

```bash
npm run electron:dev
```

语音通话需要 HTTPS 或 localhost 安全上下文以及浏览器麦克风权限。普通 HTTP IP 地址会被浏览器禁止采集麦克风；公网演示请配置 HTTPS 域名，跨 NAT 场景可能还需要 TURN。

## 测试账号
测试账号默认不创建。仅在本地演示时使用 `DEMO_SEED_USERS=true` 启动后端：
- `alice / 123456`
- `bob / 123456`
- `carol / 123456`
- `admin / 123456`

正式演示或部署前应关闭 seed，并清理这些账号或修改密码。
