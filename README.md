# MornsixQQBot | [[English Documention]](https://github.com/superwfox/MornsixQQBot/blob/master/README_EN.md)

基于 Paper API 的 Minecraft 服务器插件，通过 OneBot 协议实现 QQ 群管理功能。

---

## 项目概述

本插件通过 WebSocket 连接 OneBot 服务端，实现 Minecraft 服务器与 QQ 群的双向通信，提供群成员管理、消息审核、宵禁控制等功能。

---

## 技术架构

```
┌─────────────────┐     WebSocket      ┌─────────────────┐
│  Paper Server   │ ◄────────────────► │  OneBot Server  │
│  (Plugin Host)  │                    │  (QQ Protocol)  │
└─────────────────┘                    └─────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│                    MornsixQQBot                         │
├─────────────────────────────────────────────────────────┤
│  OneBotClient    │  事件驱动的消息处理                   │
│  CommandHandler  │  命令解析与执行                       │
│  FileManager     │  配置持久化                          │
│  Clock           │  定时任务调度                         │
└─────────────────────────────────────────────────────────┘
```

---

## 核心设计

### 事件驱动架构

采用 WebSocket 事件驱动模型而非轮询，显著降低资源消耗：

- `OneBotClient` 继承 `WebSocketClient`，通过 `onMessage` 回调处理所有事件
- 消息到达即触发处理，无需定时轮询，CPU 占用趋近于零
- 异步回调机制：使用 `CompletableFuture` + `ConcurrentHashMap` 实现请求-响应匹配

```java
// 异步请求示例：通过 echo 字段匹配响应
private static final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

public static CompletableFuture checkUser(String usage, String msgId) {
    CompletableFuture future = new CompletableFuture<>();
    pending.put(echo, future);
    client.send(json.toString());
    return future;
}
```

### 静态方法协作

类间协作采用静态方法直接调用，避免对象实例化开销：

- 无需维护对象生命周期
- 减少 GC 压力
- 调用链路清晰，便于追踪

### 定时任务优化

`Clock` 类使用 `ScheduledExecutorService` 实现分钟级精度调度：

```java
// 计算到下一分钟整点的延迟，确保精确触发
private static long computeInitialDelay() {
    return 60 - LocalDateTime.now().getSecond();
}
```

- 单线程调度器，资源占用极低
- 仅在整分钟触发检查，非持续轮询

---

## 功能列表

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
| `/file` | 上传日志 |
| `/update` | 重载配置 |

---

## 文件结构

```
plugins/MornsixQQBot/
├── superUsers.txt    # 管理员QQ列表
├── groups.txt        # 群号配置
├── shutLogs.csv      # 禁言记录
├── curfew.txt        # 宵禁时间
├── regex.txt         # 违禁正则
├── notice.txt        # 公告内容
└── mice.txt          # 黑名单
```

---

## 性能特点

| 设计选择 | 性能收益 |
|----------|----------|
| WebSocket 事件驱动 | 零轮询，消息即时响应 |
| 静态方法调用 | 无对象创建开销 |
| ConcurrentHashMap | 线程安全的异步响应匹配 |
| 单线程 ScheduledExecutor | 最小化定时任务资源占用 |
| 文件写入重试机制 | 保证数据持久化可靠性 |

---

## 依赖

- Paper API 1.21.10
- Java-WebSocket 1.5.7
- json-lib 2.4
