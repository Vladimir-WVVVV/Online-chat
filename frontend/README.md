# OnlineChat Frontend

Vue 3 + Electron PC 客户端。

## Web 调试
```bash
npm run dev
```

## Electron 调试
```bash
npm run electron:dev
```

## 构建
```bash
npm run build
```

主聊天页包含好友列表、群聊列表、聊天消息区、文件上传、好友申请、创建群聊、群成员管理、通知中心、个人资料和管理员后台。WebSocket 客户端位于 `src/websocket/client.js`，断线后会自动重连。
