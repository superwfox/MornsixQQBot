package sudark2.Sudark.mornsixQQBot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.OneBotClient.ban;
import static sudark2.Sudark.mornsixQQBot.OneBotClient.sendG;

public class CommandHandler {

    public static void curfew(String mode, Integer hours, Integer minutes) {
        if (mode.equals("on")) {
            if (hours == null) hours = 0;
            if (minutes == null) minutes = 0;

            curfewTime[0] = hours;
            curfewTime[1] = minutes;
            sendG("宵禁开始时间为" + hours + ":" + minutes, ManagerGroup);
        }

        if (mode.equals("off")) {
            if (hours == null) hours = 0;
            if (minutes == null) minutes = 0;

            curfewTime[2] = hours;
            curfewTime[3] = minutes;
            sendG("宵禁结束时间为" + hours + ":" + minutes, ManagerGroup);

        }

        if (curfewTime[0] == curfewTime[2] && curfewTime[1] == curfewTime[3])
            sendG("宵禁结束时间与开始时间相同，已关闭宵禁", ManagerGroup);
    }

    public static void regex(String mode, String regex) {
        if (mode.equals("add")) {
            Regex.add(regex);
            writeRegexList();
            sendG("已添加正则\n " + regex, ManagerGroup);
        }
        if (mode.equals("remove")) {
            Regex.remove(regex);
            writeRegexList();
            sendG("已删除正则\n " + regex, ManagerGroup);
        }
    }

    public static void shut(String userId, Integer time, String reason) {
        if (time == null) time = 0;
        if (reason == null) reason = "无";
        List<String[]> args = readShutLogs();

        args.add(new String[]{userId, time.toString(), reason, formatTime()});
        ban(userId, time, QQGroup);
        writeShutLogs(args);
        sendG("已禁言" + userId + " " + time + "秒\n原因：" + reason, ManagerGroup);
    }

    public static void unshut(String userId) {
        ban(userId, 0, QQGroup);
        sendG("已解禁" + userId, ManagerGroup);
    }

    public static void file(String UserId) {

    }

    public static void kick(String userId, String reason) {
        if (reason == null) reason = "无";
        sendG("已踢出" + userId + "\n原因：" + reason, ManagerGroup);
    }

    public static String formatTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");
        return now.format(formatter);
    }


}
