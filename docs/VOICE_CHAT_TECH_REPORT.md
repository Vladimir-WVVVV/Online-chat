# OnlineChat 1v1 语音聊天技术实现报告

## 1. 功能目标

OnlineChat 的语音聊天功能定位为课程演示用的 1v1 基础版语音通话。它不新增数据库表，不保存通话记录，只复用现有 JWT 鉴权和 STOMP WebSocket 连接完成通话信令，再由浏览器或 Electron 的 WebRTC 能力传输音频流。

当前已支持：

- 好友私聊页面发起语音通话。
- 被呼叫方在线时收到来电弹窗。
- 被呼叫方可接听或拒绝。
- 接听后双方进入通话状态。
- 双方均可挂断。
- 显示呼叫中、来电中、通话中、已挂断、对方拒绝、对方不在线或连接失败等状态。
- 麦克风权限、STUN 不可用、WebRTC 连接失败时给出前端错误提示，不影响文字聊天。

## 2. 总体架构

语音聊天分为两层：

1. 信令层：使用现有 Spring STOMP WebSocket。
2. 媒体层：使用浏览器或 Electron 提供的 WebRTC `RTCPeerConnection`。

信令层负责传递通话控制消息和 WebRTC 协商数据，包括：

- `CALL`：发起呼叫。
- `ACCEPT`：接听。
- `REJECT`：拒绝。
- `OFFER`：WebRTC offer SDP。
- `ANSWER`：WebRTC answer SDP。
- `ICE`：ICE candidate。
- `HANGUP`：挂断。
- `ERROR`：后端返回的失败状态，例如对方不在线、非好友。

媒体层由双方浏览器直接建立点对点连接，后端不转发音频流。

## 3. 后端实现

### 3.1 相关文件

后端语音模块位于：

```text
backend/src/main/java/com/whu/onlinechat/voice/
├── VoiceSignalDTO.java
├── VoiceService.java
├── VoiceServiceImpl.java
└── VoiceWebSocketController.java
```

### 3.2 STOMP 入口

`VoiceWebSocketController` 提供以下 `@MessageMapping`：

```text
/app/voice.call
/app/voice.accept
/app/voice.reject
/app/voice.offer
/app/voice.answer
/app/voice.ice
/app/voice.hangup
```

所有消息最终委托给 `VoiceService.forward(...)`，并强制指定信令类型，避免前端伪造不匹配的 `type`。

### 3.3 信令 DTO

`VoiceSignalDTO` 结构：

```json
{
  "type": "CALL",
  "fromUserId": 1,
  "toUserId": 2,
  "fromUsername": "alice",
  "targetUserId": 2,
  "sdp": "...",
  "candidate": {},
  "message": "错误信息",
  "timestamp": "2026-05-16T10:00:00"
}
```

其中：

- `fromUserId/fromUsername` 由后端根据 STOMP 登录用户补全。
- `toUserId` 是接收方用户 ID。
- `sdp` 用于 `OFFER/ANSWER`。
- `candidate` 用于 `ICE`。
- `message` 用于 `ERROR`。
- `timestamp` 由后端生成。

### 3.4 鉴权与安全校验

语音信令复用现有 WebSocket 鉴权流程：

- STOMP CONNECT 时必须携带 `Authorization: Bearer <JWT>`。
- `WebSocketConfig` 解析 JWT 后设置 `UserPrincipal`。
- 如果账号已封禁，连接阶段会被拒绝。

`VoiceServiceImpl` 在每条语音信令转发前继续做业务校验：

- 调用 `userService.requireActiveUser(user.id())`，封禁用户不能发起或响应语音。
- 检查 `toUserId` 是否存在。
- 调用 `friendService.isFriend(...)`，只允许好友之间通话。
- 调用 `onlineUserService.isOnline(...)`，对方离线时返回 `ERROR`。

语音信令不写入数据库，不影响 `message`、`notification` 等已有表。

### 3.5 信令转发

校验通过后，后端使用：

```java
messagingTemplate.convertAndSendToUser(
    String.valueOf(toUserId),
    "/queue/voice",
    outbound
);
```

前端订阅：

```text
/user/queue/voice
```

这样信令只投递给目标用户，不广播到群聊 topic，也不会影响已有文字消息订阅 `/user/queue/messages`。

## 4. 前端实现

### 4.1 相关文件

前端语音模块位于：

```text
frontend/src/voice/voiceService.js
frontend/src/views/ChatHomeView.vue
```

`voiceService.js` 负责 WebRTC 和 STOMP 信令封装；`ChatHomeView.vue` 负责按钮、弹窗、状态浮层和当前会话判断。

### 4.2 UI 入口

聊天输入区新增“语音”按钮：

- 仅当当前会话是 `PRIVATE` 且 STOMP 已连接时可用。
- 群聊会话中禁用。
- 点击后发起 `CALL` 信令。

收到来电后显示 Element Plus 弹窗：

```text
Alice 邀请你语音通话
接听 / 拒绝
```

通话过程中显示浮层：

- 当前通话对象。
- 当前通话状态。
- 挂断按钮。

### 4.3 WebRTC 初始化

前端使用基础 STUN 配置：

```js
new RTCPeerConnection({
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' }
  ]
})
```

获取麦克风：

```js
navigator.mediaDevices.getUserMedia({ audio: true })
```

如果用户拒绝权限或当前环境不支持麦克风采集，前端显示“麦克风权限被拒绝”或“当前环境不支持麦克风采集”。

### 4.4 呼叫流程

Alice 呼叫 Bob 的流程如下：

1. Alice 点击“语音”。
2. Alice 前端调用 `getUserMedia({ audio: true })`。
3. Alice 通过 STOMP 发送 `/app/voice.call`。
4. 后端校验 Alice 是否封禁、Bob 是否好友、Bob 是否在线。
5. 后端向 Bob 的 `/user/queue/voice` 转发 `CALL`。
6. Bob 前端显示来电弹窗。

### 4.5 接听与 SDP 协商流程

Bob 接听后的流程：

1. Bob 点击“接听”。
2. Bob 创建 `RTCPeerConnection`，采集本地麦克风。
3. Bob 发送 `/app/voice.accept`。
4. Alice 收到 `ACCEPT` 后创建 offer。
5. Alice 发送 `/app/voice.offer`，其中包含 offer SDP。
6. Bob 收到 `OFFER` 后执行 `setRemoteDescription`。
7. Bob 创建 answer，并发送 `/app/voice.answer`。
8. Alice 收到 `ANSWER` 后执行 `setRemoteDescription`。
9. 双方通过 `/app/voice.ice` 交换 ICE candidate。
10. WebRTC 连接建立后，双方进入“通话中”状态。

### 4.6 拒绝与挂断

拒绝流程：

1. Bob 点击“拒绝”。
2. Bob 发送 `/app/voice.reject`。
3. Alice 收到 `REJECT`。
4. Alice 停止本地音频 track，关闭 `RTCPeerConnection`，状态显示“对方拒绝”。

挂断流程：

1. 任一方点击“挂断”。
2. 前端发送 `/app/voice.hangup`。
3. 对方收到 `HANGUP`。
4. 双方停止本地音频 track，关闭 `RTCPeerConnection`，清理远端 audio。

清理动作包括：

- `peer.close()`。
- `localStream.getTracks().forEach(track => track.stop())`。
- 清空远端 `Audio.srcObject`。
- 重置当前通话对象和状态。

## 5. 状态设计

前端维护两个状态对象：

```js
voice = {
  visible: false,
  status: '已挂断',
  peerId: null,
  peerName: ''
}

incomingCall = {
  visible: false,
  fromUserId: null,
  fromUsername: ''
}
```

状态变化示例：

| 场景 | 发起方状态 | 接收方状态 |
| --- | --- | --- |
| 发起呼叫 | 呼叫中 | - |
| 收到来电 | - | 来电中 |
| 接听成功 | 通话中 | 通话中 |
| 拒绝 | 对方拒绝 | 已挂断 |
| 挂断 | 已挂断 | 已挂断 |
| 对方离线 | 对方不在线或连接失败 | - |
| WebRTC 异常 | 连接失败 | 连接失败 |

## 6. 与现有聊天功能的关系

语音功能的设计原则是最小侵入：

- 不修改已有聊天消息发送目的地。
- 不修改私聊、群聊、文件、通知、后台管理的业务流程。
- 不新增数据库表。
- 不保存通话记录。
- 不使用 `message` 表存储语音信令。
- 前端只在 `ChatHomeView.vue` 增加语音入口、弹窗和浮层。

文字聊天仍使用：

```text
/app/chat.private
/app/chat.group
/user/queue/messages
/topic/groups/{groupId}
```

语音信令独立使用：

```text
/app/voice.*
/user/queue/voice
```

## 7. 异常处理

### 7.1 后端异常

后端对以下情况返回 `ERROR` 信令：

- 目标用户为空。
- 非好友通话。
- 对方不在线。

封禁用户无法建立 WebSocket 连接；如果连接后状态变化，业务层的 `requireActiveUser` 仍会拦截。

### 7.2 前端异常

前端对以下情况使用 Element Plus Message 提示：

- 麦克风权限被拒绝。
- 当前环境不支持 `getUserMedia`。
- STOMP 未连接。
- WebRTC SDP 或 ICE 处理失败。
- 后端返回 `ERROR`。

即使真实音频流因为权限、Electron 环境或网络限制未能建立，来电、接听、拒绝、挂断和状态变化仍可演示。

## 8. 测试与验收

### 8.1 自动测试

已补充语音相关测试：

- `VoiceSignalDTOTest`：验证 DTO 补全发送方和时间戳。
- `VoiceServiceImplTest`：验证非好友不能发起语音。

运行：

```bash
cd backend
mvn test
```

### 8.2 手动验收

建议按以下流程验收：

1. 启动后端、前端。
2. 使用两个浏览器窗口或 Electron + 浏览器分别登录 Alice 和 Bob。
3. 确认 Alice 和 Bob 是好友。
4. Alice 打开 Bob 私聊页面。
5. Alice 点击“语音”。
6. Bob 收到来电弹窗。
7. Bob 点击“接听”。
8. 双方显示“通话中”。
9. Alice 点击“挂断”，双方状态结束。
10. 再次呼叫，Bob 点击“拒绝”，Alice 显示“对方拒绝”。
11. 退出 Bob 或断开 Bob WebSocket，再由 Alice 呼叫，Alice 应收到“对方不在线或连接失败”。

## 9. 当前限制与后续优化

当前实现是 1v1 基础版，主要面向课程本地演示，存在以下限制：

- 不支持多人语音房间。
- 不保存通话记录。
- 不统计通话时长。
- 不支持忙线检测，同一用户多端同时响铃时由当前在线连接处理。
- 仅配置公共 STUN，公网 NAT 穿透不稳定时需要 TURN 服务。
- Electron 或浏览器麦克风权限受限时，真实音频流可能不可用。

后续可扩展方向：

- 增加 TURN 配置项。
- 增加忙线状态和通话超时。
- 增加通话记录表或复用通知记录呼叫结果。
- 增加静音、扬声器选择、音量提示。
- 增加更完善的 WebRTC 连接状态展示。
