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

## 前端
```bash
cd /home/genevievelin/Online-chat/onlinechat/frontend
npm run dev
```

浏览器访问 Vite 输出地址，默认 `http://localhost:5173`。Electron 演示可运行：

```bash
npm run electron:dev
```

## 测试账号
- `alice / 123456`
- `bob / 123456`
- `carol / 123456`
- `admin / 123456`
