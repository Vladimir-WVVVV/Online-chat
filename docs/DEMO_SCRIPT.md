# Demo Script

1. 打开前端，使用 `alice / 123456` 登录。
2. 在左侧点击“添加好友”，搜索 `bob`，向 bob 发送好友申请。
3. 另开窗口或退出后使用 `bob / 123456` 登录，打开“好友申请”，接受 alice 的申请。
4. alice 回到好友列表，点击 bob，发送私聊文本消息。
5. alice 发送一条新消息后，在 2 分钟内点击“撤回”，双方消息区显示“消息已撤回”。
6. alice 点击“创建群聊”，创建课堂演示群；bob、carol 登录后通过群 ID 调用加入按钮所在接口或由演示数据准备加入。
7. alice、bob、carol 分别进入该群聊，发送群消息，展示实时广播。
8. 在消息输入区点击“文件”，发送图片或普通文件；图片可预览，普通文件可下载。
9. 查看右侧“通知”，展示好友申请、私聊、群聊通知；打开会话后未读角标清除。
10. 使用 `admin / 123456` 登录，打开右侧“后台”，查看用户列表、在线人数、今日消息数、今日新增用户数，并封禁/解封普通用户。
11. 打开 `http://localhost:8080/swagger-ui.html` 展示接口文档。
12. 展示数据库 9 张核心表：`user/friendship/friend_request/chat_group/group_member/message/file_record/notification/admin_log`。

本轮没有 AI、电话、语音连麦、WebRTC 入口，也没有新增相关表。
