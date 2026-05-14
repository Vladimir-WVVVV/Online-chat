# API

统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

已实现：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/users/me`
- `PUT /api/users/me`

认证和用户资料接口当前只依赖 `user` 表。好友、单聊、群聊、AI 和连麦相关业务接口尚未在本轮实现。

Swagger：`http://localhost:8080/swagger-ui/index.html`
