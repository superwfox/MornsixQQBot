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
│  OneBotClient    │  Event-driven message handling       │
│  CommandHandler  │  Command parsing & execution         │
│  FileManager     │  Configuration persistence           │
│  Clock           │  Scheduled task management           │
└─────────────────────────────────────────────────────────┘
```

---

## Core Design

### Event-Driven Architecture

Uses WebSocket event-driven model instead of polling, significantly reducing resource consumption:

- `OneBotClient` extends `WebSocketClient`, handles all events via `onMessage` callback
- Messages trigger processing immediately, no polling required, near-zero CPU usage
- Async callback mechanism: `CompletableFuture` + `ConcurrentHashMap` for request-response matching

```java
// Async request example: matching responses via echo field
private static final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

public static CompletableFuture checkUser(String usage, String msgId) {
    CompletableFuture future = new CompletableFuture<>();
    pending.put(echo, future);
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

---

## Performance

| Design Choice | Performance Benefit |
|---------------|---------------------|
| WebSocket Event-Driven | Zero polling, instant response |
| Static Method Calls | No object creation overhead |
| ConcurrentHashMap | Thread-safe async response matching |
| Single-thread ScheduledExecutor | Minimal scheduler resource usage |
| File Write Retry | Reliable data persistence |

---

## Dependencies

- Paper API 1.21.10
- Java-WebSocket 1.5.7
- json-lib 2.4
