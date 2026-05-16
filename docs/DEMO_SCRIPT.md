# Demo Script

1. 打开前端，使用 `alice / 123456` 登录。
2. 在左侧点击“添加好友”，搜索 `bob`，向 bob 发送好友申请。
3. 另开窗口或退出后使用 `bob / 123456` 登录，打开“好友申请”，接受 alice 的申请。
4. alice 回到好友列表，点击 bob，发送私聊文本消息。
5. alice 发送一条新消息后，在 2 分钟内点击“撤回”，双方消息区显示“消息已撤回”。
6. 确保 alice 已经和 bob、carol 是好友；alice 点击“创建群聊”，输入群名 `test`，勾选 bob 和 carol 后创建。
7. bob、carol 登录后在群聊列表看到 `test`，alice、bob、carol 分别进入该群聊，发送群消息，展示实时广播。
8. 在消息输入区点击“文件”，发送图片或普通文件；图片可预览，普通文件可下载。
9. 查看右侧“通知”，展示好友申请、私聊、群聊通知；打开会话后未读角标清除。
10. 使用 `admin / 123456` 登录，打开右侧“后台”，查看用户列表、在线人数、今日消息数、今日新增用户数，并封禁/解封普通用户。
11. 打开 `http://localhost:8080/swagger-ui.html` 展示接口文档。
12. 展示数据库 9 张核心表：`user/friendship/friend_request/chat_group/group_member/message/file_record/notification/admin_log`。
13. Alice 和 Bob 保持好友关系，Alice 在与 Bob 的私聊中输入 `@AI WebSocket 是什么`，展示“AI 正在思考中...”和答疑助手回复。
14. Alice 在群聊中输入 `/summary`，展示总结助手对最近消息的总结；消息较少时展示“当前消息较少，暂无可总结内容”。
15. Alice 点击“氛围助手”，展示轻量互动回复。
16. Alice 在 Bob 私聊页面点击“语音”，Bob 在线窗口收到“alice 邀请你语音通话”弹窗。
17. Bob 点击“接听”，双方浮层显示“通话中”；Alice 点击“挂断”，双方状态结束。
18. 再演示一次 Alice 呼叫 Bob，Bob 点击“拒绝”，Alice 侧显示“对方拒绝”。

AI 默认 Mock 模式，不需要真实大模型 Key。语音通话需要浏览器或 Electron 授权麦克风；如音频权限受限，仍可演示来电、接听、拒绝、挂断和状态变化。
