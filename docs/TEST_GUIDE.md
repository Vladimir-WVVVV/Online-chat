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

课堂演示优先使用前端界面完成好友、私聊、群聊、撤回、文件、通知和管理员后台流程。
