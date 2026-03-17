# MornsixQQBot

基于 Paper API 的 Minecraft 服务器插件，通过 OneBot 协议实现 QQ 群管理功能。

---

## 项目概述

本插件通过 WebSocket 连接 OneBot 服务端，实现 Minecraft 服务器与 QQ 群的双向通信，提供群成员管理、消息审核、宵禁控制等功能。附带 B站动态嗅探模块，可抓取用户最新动态并生成图片。支持 Outlook 邮箱监控，自动转发新邮件到管理群。

---

## 主要功能

| 命令 | 功能 |
|------|------|
| `/ban <qq> <秒> [原因]` | 禁言用户 |
| `/unban <qq>` | 解除禁言 |
| `/kick <qq> [原因]` | 踢出用户 |
| `/admin add/remove <qq>` | 管理员管理 |
| `/curfew on/off <时> <分>` | 宵禁设置 |
| `/regex add/remove/list [正则]` | 违禁词管理 |
| `/setmice add/remove <qq>` | 黑名单管理 |
| `/setnotice <内容>` | 设置公告 |
| `/adduid <uid>` | 添加B站监控 |
| `/removeuid <uid>` | 移除B站监控 |
| `/checkuid` | 查看监控列表 |
| `/setemail <邮箱> <应用密码>` | 配置邮箱监控（IMAP/SMTP） |
| `/file` | 上传日志 |
| `/update` | 重载配置 |

管理群内回复操作：

```text
回复违规转发消息：
ban 600 广告刷屏

回复机器人"[回复此消息补充]"禁言消息：
这里补充禁言备注
```

---

## 项目结构

```
src/main/java/sudark2/Sudark/mornsixQQBot/
├── MornsixQQBot.java                  # 插件入口与生命周期
├── FileManager.java                   # 配置持久化（文件读写）
│
├── onebot/                            # OneBot 协议层
│   ├── OneBotClient.java              # WebSocket 客户端
│   ├── OneBotApi.java                 # OneBot Action 发送封装
│   ├── OneBotEchoStore.java           # echo 异步回包匹配
│   ├── OneBotEventRouter.java         # 事件路由（群聊/私聊/通知）
│   └── OneBotReplyHandler.java        # 回复命令与禁言备注匹配
│
├── command/                           # 命令处理
│   ├── BanCommands.java               # 禁言/解禁/踢人/备注
│   └── AdminCommands.java             # 管理员/宵禁/正则/公告/黑名单/B站监控
│
├── schedule/                          # 定时任务
│   └── Clock.java                     # 宵禁调度、公告推送、B站动态检查
│
├── BiliDataSniffer/                   # B站动态嗅探
│   ├── BiliData.java                  # 动态数据获取与补全
│   ├── BiliChecker.java               # 定时检查 + 图片/卡片/链接推送
│   ├── HttpsHandler.java              # HTTP 请求（空间/详情接口）
│   ├── PictureGen.java                # 动态图片生成（布局）
│   └── DrawUtil.java                  # 绘图工具（字体/裁切/文本换行）
│
└── EmailRelated/                      # 邮件监控（IMAP/SMTP）
    ├── EmailConfig.java               # 邮箱配置管理
    ├── ImapSmtpClient.java            # IMAP收件 + SMTP发件客户端
    ├── EmailMessage.java              # 邮件数据模型
    ├── EmailFormatter.java            # 邮件格式化为QQ消息
    └── EmailChecker.java              # 定时邮件扫描
```

### 运行时文件

```
plugins/MornsixQQBot/
├── superUsers.txt    # 管理员QQ列表
├── groups.txt        # 群号配置
├── shutLogs.csv      # 禁言记录
├── curfew.txt        # 宵禁时间
├── regex.txt         # 违禁正则
├── notice.txt        # 公告内容
├── mice.txt          # 黑名单
├── biliUids.txt      # B站监控UID列表
└── email_config.txt  # 邮箱配置（邮箱|密码|imapHost|imapPort|smtpHost|smtpPort）
```

---

## 技术架构

```
┌─────────────────┐     WebSocket      ┌─────────────────┐
│  Paper Server   │ ◄────────────────► │  OneBot Server  │
│  (Plugin Host)  │                    │  (QQ Protocol)  │
└─────────────────┘                    └─────────────────┘
```

- **事件驱动**：WebSocket 推送消息，零轮询，CPU 占用趋近于零
- **异步回调**：`CompletableFuture` + `ConcurrentHashMap` 实现请求-响应匹配
- **静态方法协作**：类间通过静态方法直接调用，无对象实例化开销
- **分钟级调度**：`ScheduledExecutorService` 单线程在整分钟触发宵禁检查

---

## 使用方法

### 1. 部署

将编译后的 JAR 放入 Paper 服务端 `plugins/` 目录，确保 OneBot 服务端在 `ws://127.0.0.1:3001` 监听。

### 2. 首次配置

管理员私聊机器人设置群号：

```text
/setgroup <业务群号> <管理群号>
```

对应 `FileManager.writeGroupList()`，将群号写入 `groups.txt` 并持久化。

### 3. 常用管理命令

```text
/ban 123456 600 广告刷屏      → BanCommands.shut()
/unban 123456                 → BanCommands.unshut()
/kick 123456 多次引战          → BanCommands.kick()
/regex add (测试违禁词)        → AdminCommands.regex()
/curfew on 23 30              → AdminCommands.curfew()
/setnotice 本周六19:00维护公告  → AdminCommands.setNotice()
/setemail user@outlook.com app_password → AdminCommands.setEmail()
```

### 4. 邮件监控（IMAP/SMTP）

配置邮箱后，系统每 5 分钟通过 IMAP 扫描未读邮件并转发到管理群：

```text
/setemail your@outlook.com <应用密码>
# 自定义邮件服务器：
/setemail 邮箱 密码 imapHost imapPort smtpHost smtpPort
```

- Outlook 用户需在账户安全设置中生成应用密码
- 默认使用 Outlook 服务器，也支持自定义 IMAP/SMTP 服务器
- 支持文本内容预览（前 500 字符）
- 自动解析并发送图片附件（最多 5 张，≤10MB）
- IMAP SEEN flag 标记已读，避免重复推送
- 登录失败时自动通知管理员检查密码
- SMTP 发件功能已预留

命令由 `OneBotEventRouter.handlePrivateMessage()` 解析路由，分派到 `command` 包下的具体方法执行。

---

## 构建

```bash
mvn clean package
```

> 注意：`pom.xml` 中 resources 的 `filtering` 对 `.ttf` 字体文件关闭，避免 `MalformedInputException`。

---

## 依赖

- Paper API 1.21.10
- Java-WebSocket 1.5.7
- json-lib 2.4
- Jakarta Mail 2.0.1
