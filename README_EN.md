# MornsixQQBot

A Minecraft server plugin based on Paper API that implements QQ group management via OneBot protocol.

---

## Overview

This plugin connects to OneBot server via WebSocket, enabling bidirectional communication between Minecraft server and QQ groups, providing member management, message moderation, and curfew control.

---

## Architecture

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
│  OneBotClient    │  WebSocket entry and lifecycle       │
│  OneBotApi       │  OneBot action sending               │
│  OneBotEchoStore │  Echo async response state           │
│  OneBotEventRouter│ Group/private/notice event routing  │
│  OneBotReplyHandler│ Reply command + strict mute remark │
│  CommandHandler  │  Command parsing & execution         │
│  FileManager     │  Configuration persistence           │
│  Clock           │  Scheduled task management           │
└─────────────────────────────────────────────────────────┘
```

---

## Core Design

### Event-Driven Architecture

Uses WebSocket event-driven model instead of polling, significantly reducing resource consumption:

- `OneBotClient` extends `WebSocketClient`, focused on socket lifecycle and message entry
- `OneBotEventRouter` dispatches events by type (request/group/private/notice)
- `OneBotApi` centralizes OneBot action sending
- `OneBotEchoStore` manages echo response matching and async completion
- Messages trigger processing immediately, no polling required, near-zero CPU usage
- Async callback mechanism: `CompletableFuture` + `ConcurrentHashMap` for request-response matching

```java
// Async request example: matching responses via echo field
private static final ConcurrentHashMap<String, CompletableFuture<Pair<String, JSONArray>>> pending = new ConcurrentHashMap<>();

public static CompletableFuture<Pair<String, JSONArray>> checkUser(String msgId) {
    CompletableFuture<Pair<String, JSONArray>> future = new CompletableFuture<>();
    pending.put(msgId, future);
    client.send(json.toString());
    return future;
}
```

### Static Method Collaboration

Inter-class collaboration uses static method calls, avoiding object instantiation overhead:

- No object lifecycle management needed
- Reduced GC pressure
- Clear call chains, easy to trace

### Scheduled Task Optimization

`Clock` class uses `ScheduledExecutorService` for minute-level precision scheduling:

```java
// Calculate delay to next minute boundary for precise triggering
private static long computeInitialDelay() {
    return 60 - LocalDateTime.now().getSecond();
}
```

- Single-thread scheduler, minimal resource usage
- Only triggers at minute boundaries, not continuous polling

---

## Features

| Command | Function |
|---------|----------|
| `/ban <qq> <seconds> [reason]` | Ban user |
| `/unban <qq>` | Unban user |
| `/kick <qq> [reason]` | Kick user |
| `/admin add/remove <qq>` | Admin management |
| `/curfew on/off <hour> <minute>` | Curfew control |
| `/regex add/remove/list [pattern]` | Regex filter |
| `/setmice add/remove <qq>` | Blacklist management |
| `/setnotice <content>` | Set announcement |
| `/file` | Upload logs |
| `/update` | Reload config |

---

## Usage

### 1. Initial Group Setup

Send private command as admin:

```text
/setgroup <business_group_id> <manager_group_id>
```

### 2. Common Admin Commands

```text
/ban 123456 600 spam
/unban 123456
/kick 123456 repeated trolling
/regex add (blocked_pattern)
/setnotice Maintenance at 19:00 this Saturday
```

### 3. Reply-Based Operations in Manager Group

```text
Reply to forwarded violation message:
ban 600 spam

Reply to bot mute message with [reply to add reason]:
add mute reason here
```

---

## File Structure

```
plugins/MornsixQQBot/
├── superUsers.txt    # Admin QQ list
├── groups.txt        # Group ID config
├── shutLogs.csv      # Ban records
├── curfew.txt        # Curfew time
├── regex.txt         # Regex patterns
├── notice.txt        # Announcement
└── mice.txt          # Blacklist
```

### Source Structure

```
src/main/java/sudark2/Sudark/mornsixQQBot/
├── OneBotClient.java                # WebSocket entry
├── onebot/OneBotApi.java            # OneBot action wrapper
├── onebot/OneBotEchoStore.java      # Echo async response state
├── onebot/OneBotEventRouter.java    # Event routing
├── onebot/OneBotReplyHandler.java   # Reply command and mute-remark matching
├── CommandHandler.java              # Command handling
├── FileManager.java                 # File IO and config loading
└── Clock.java                       # Scheduled jobs
```

---

## Performance

| Design Choice | Performance Benefit |
|---------------|---------------------|
| WebSocket Event-Driven | Zero polling, instant response |
| Static Method Calls | No object creation overhead |
| ConcurrentHashMap | Thread-safe async response matching |
| Single-thread ScheduledExecutor | Minimal scheduler resource usage |
| Fail-fast + admin notification | Lower complexity with clear failure signals |

---

## Dependencies

- Paper API 1.21.10
- Java-WebSocket 1.5.7
- json-lib 2.4
