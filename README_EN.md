# MornsixQQBot

A Minecraft server plugin based on Paper API that implements QQ group management via OneBot protocol.

---

## Overview

This plugin connects to OneBot server via WebSocket, enabling bidirectional communication between Minecraft server and QQ groups, providing member management, message moderation, and curfew control. Includes Bilibili dynamic monitoring and Outlook email forwarding to manager group.

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
| `/adduid <uid>` | Add Bilibili monitoring |
| `/removeuid <uid>` | Remove Bilibili monitoring |
| `/checkuid` | Check monitoring list |
| `/setemail <email> <app_password>` | Configure email monitoring (IMAP/SMTP) |
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
/setemail user@outlook.com app_password
# Custom mail server:
/setemail email password imapHost imapPort smtpHost smtpPort
```

### 4. Email Monitoring (IMAP/SMTP)

After configuring email, system scans unread emails every 5 minutes via IMAP and forwards to manager group:

```text
/setemail your@outlook.com <app_password>
# Custom mail server:
/setemail email password imapHost imapPort smtpHost smtpPort
```

- Outlook users need to generate an app password in account security settings
- Defaults to Outlook servers, also supports custom IMAP/SMTP servers
- Supports text preview (first 500 characters)
- Auto-parses and sends image attachments (up to 5, ≤10MB)
- IMAP SEEN flag marks as read to prevent duplicates
- Notifies admin on login failure
- SMTP sending reserved for future use

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
├── mice.txt          # Blacklist
├── biliUids.txt      # Bilibili UID monitoring list
└── email_config.txt  # Email config (email|password|imapHost|imapPort|smtpHost|smtpPort)
```

### Source Structure

```
src/main/java/sudark2/Sudark/mornsixQQBot/
├── MornsixQQBot.java                # Plugin entry
├── FileManager.java                 # File IO and config loading
├── onebot/                          # OneBot protocol layer
│   ├── OneBotClient.java            # WebSocket client
│   ├── OneBotApi.java               # OneBot action wrapper
│   ├── OneBotEchoStore.java         # Echo async response state
│   ├── OneBotEventRouter.java       # Event routing
│   └── OneBotReplyHandler.java      # Reply command handling
├── command/                         # Command handlers
│   ├── BanCommands.java             # Ban/unban/kick
│   └── AdminCommands.java           # Admin/curfew/regex/notice/blacklist/bili/email
├── schedule/                        # Scheduled tasks
│   └── Clock.java                   # Curfew/notice/bili/email scheduling
├── BiliDataSniffer/                 # Bilibili dynamic monitoring
│   ├── BiliData.java                # Dynamic data fetching
│   ├── BiliChecker.java             # Scheduled checker
│   ├── HttpsHandler.java            # HTTP requests
│   ├── PictureGen.java              # Image generation
│   └── DrawUtil.java                # Drawing utilities
└── EmailRelated/                    # Email monitoring (IMAP/SMTP)
    ├── EmailConfig.java             # Email config management
    ├── ImapSmtpClient.java          # IMAP receive + SMTP send client
    ├── EmailMessage.java            # Email data model
    ├── EmailFormatter.java          # Format email as QQ message
    └── EmailChecker.java            # Scheduled email scanner
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
- Jakarta Mail 2.0.1
