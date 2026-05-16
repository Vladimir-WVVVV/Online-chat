# Test Guide

## 自动测试
```bash
cd backend
mvn test

cd ../frontend
npm run build
```

`mvn test` 使用 H2 内存库 profile，不影响正式 MySQL 配置。

## 手动验收接口
先登录获取 token：
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"123456"}'
```

好友申请：
```bash
curl -X POST http://localhost:8080/api/friends/requests \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"toUserId":2,"message":"请求添加好友"}'
```

创建群聊并邀请好友：
```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"test","description":"课程演示群","memberIds":[2,3]}'
```

前端手动验收时，Alice 点击“创建群聊”，输入群名 `test`，勾选 Bob 和 Carol；Bob、Carol 重新登录或刷新后应能在群聊列表看到 `test`。

管理员接口普通用户应被拒绝：
```bash
curl http://localhost:8080/api/admin/users -H "Authorization: Bearer $ALICE_TOKEN"
```

文件上传 20MB 限制：
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -F "file=@./demo.png"
```

AI Mock 调用：
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"conversationType":"PRIVATE","targetId":2,"agentType":"QA","content":"WebSocket 是什么"}'
```

语音手动验收：
- Alice 和 Bob 必须是好友并同时在线。
- Alice 打开 Bob 私聊，点击“语音”。
- Bob 收到来电弹窗后接听，双方显示“通话中”。
- 任一方点击挂断后状态结束。
- 再测试 Bob 拒绝来电，Alice 显示“对方拒绝”。

当前补充的自动测试包括：
- AI Mock 回复测试。
- 语音 DTO 基础测试。
- 非好友不能发起语音测试。

课堂演示优先使用前端界面完成好友、私聊、群聊、撤回、文件、AI、语音、通知和管理员后台流程。
