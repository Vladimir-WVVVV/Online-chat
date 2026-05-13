# Run Guide

## 1. 启动 MySQL 和 Redis

```bash
cd onlinechat/deploy
docker compose up -d mysql redis
```

## 2. 启动后端

```bash
cd onlinechat/backend
mvn spring-boot:run
```

## 3. 启动前端

```bash
cd onlinechat/frontend
npm install
npm run dev
```

## 4. 启动 Electron

```bash
cd onlinechat/frontend
npm run electron:dev
```

