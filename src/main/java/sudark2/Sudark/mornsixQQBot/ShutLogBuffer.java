package sudark2.Sudark.mornsixQQBot;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.command.BanCommands.getShutTime;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendG;

public class ShutLogBuffer {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ShutLogBuffer");
        t.setDaemon(true);
        return t;
    });

    private static final ConcurrentHashMap<String, BufferedEntry> buffer = new ConcurrentHashMap<>();

    private static class BufferedEntry {
        final String[] record;
        final ScheduledFuture<?> future;

        BufferedEntry(String[] record, ScheduledFuture<?> future) {
            this.record = record;
            this.future = future;
        }
    }

    private static boolean isShutType(String[] record) {
        return "禁言".equals(record[0]);
    }

    /**
     * 添加记录到缓冲区。
     * 禁言↔解禁互相抵消，同类型覆盖旧记录。
     * 返回: "buffered" 正常缓冲, "cancelled" 已抵消
     */
    public static String add(String[] record) {
        String userId = record[1];

        BufferedEntry existing = buffer.get(userId);
        if (existing != null) {
            existing.future.cancel(false);
            boolean existingIsShut = isShutType(existing.record);
            boolean newIsShut = isShutType(record);

            if (existingIsShut != newIsShut) {
                buffer.remove(userId);
                return "cancelled";
            }
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> flush(userId), 5, TimeUnit.MINUTES);
        buffer.put(userId, new BufferedEntry(record, future));
        return "buffered";
    }

    private static void flush(String userId) {
        BufferedEntry entry = buffer.remove(userId);
        if (entry == null) return;

        String[] record = entry.record;
        List<String[]> args = readShutLogs();
        args.add(record);
        writeShutLogs(args);

        if (isShutType(record)) {
            int banTime = getShutTime(args, userId);
            sendG("[已写入] 禁言 [" + userId + "] " + record[2] + "秒\n原因：" + record[3]
                    + "\n处理者： " + record[5] + "\n总禁言次数 ： " + banTime, ManagerGroup);
        } else {
            sendG("[已写入] 解禁 [" + userId + "]\n处理者： " + record[5], ManagerGroup);
        }
    }
}
