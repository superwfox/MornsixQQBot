package sudark2.Sudark.mornsixQQBot.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.ShutLogBuffer.add;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.*;

public class BanCommands {

    public static void shut(String userId, Integer time, String reason, String askId) {
        if (time == null)
            time = 0;
        if (reason == null)
            reason = "无";
        String[] record = new String[] { "禁言", userId, time.toString(), reason, formatTime(), askId };
        ban(userId, time, QQGroup);
        String result = add(record);
        if ("cancelled".equals(result)) {
            sendG("[已抵消] 禁言/解禁 [" + userId + "] 操作互相抵消，不写入记录", ManagerGroup);
        } else {
            sendG("[暂定] 已禁言 [" + userId + "] " + time + "秒\n原因：" + reason + "\n处理者： " + askId, ManagerGroup);
            sendG("已禁言 [" + userId + "] " + time + "秒\n原因：" + reason, QQGroup);
        }
    }

    public static void shutAuto(String userId, String time, String askId) {
        String reason = "[回复此消息补充]";
        String[] record = new String[] { "禁言", userId, time, "无", formatTime(), askId };
        String result = add(record);
        if ("cancelled".equals(result)) {
            sendG("[已抵消] 禁言/解禁 [" + userId + "] 操作互相抵消，不写入记录", ManagerGroup);
        } else {
            sendG("[暂定] 已禁言 [" + userId + "] " + time + "秒\n原因：" + reason + "\n处理者： " + askId, ManagerGroup);
        }
    }

    public static void unShutAuto(String userId, String askId) {
        String[] record = new String[] { "解禁", userId, "0", "无", formatTime(), askId };
        String result = add(record);
        if ("cancelled".equals(result)) {
            sendG("[已抵消] 禁言/解禁 [" + userId + "] 操作互相抵消，不写入记录", ManagerGroup);
        } else {
            sendG("[暂定] 已解禁 [" + userId + "]\n处理者： " + askId, ManagerGroup);
        }
    }

    public static void unshut(String userId, String askId) {
        String[] record = new String[] { "解禁", userId, "0", "无", formatTime(), askId };
        ban(userId, 0, QQGroup);
        String result = add(record);
        if ("cancelled".equals(result)) {
            sendG("[已抵消] 禁言/解禁 [" + userId + "] 操作互相抵消，不写入记录", ManagerGroup);
        } else {
            sendG("[暂定] 已解禁 [" + userId + "]\n处理者： " + askId, ManagerGroup);
            sendG("已解禁 [" + userId + "]", QQGroup);
        }
    }

    public static void kick(String userId, String reason, String askId) {
        if (reason == null)
            reason = "无";
        List<String[]> args = readShutLogs();
        args.add(new String[] { "踢出", userId, "0", reason, formatTime(), askId });
        writeShutLogs(args);
        kickG(userId);
        sendG("已踢出" + userId + "\n原因：" + reason + "\n处理者： " + askId, ManagerGroup);
    }

    public static void replyMakeup(String qq, String reason, String askId) {
        List<String[]> list = readShutLogs();
        int latest = 0;
        int loc = -1;

        for (String[] line : list) {
            if (line[1].equals(qq) && line[0].equals("禁言")) {
                latest = list.indexOf(line);
                loc = list.indexOf(line);
            }
        }

        if (loc == -1) {
            sendP(askId, "该用户没有禁言记录");
            return;
        }

        String[] target = list.get(latest);
        list.set(loc, new String[] { target[0], target[1], target[2], reason, target[4], askId });
        writeShutLogs(list);
        sendG("·将 [" + qq + "] \n" + target[4] + "\n\n禁言原因修改为: " + reason + "\n·处理者： " + askId, ManagerGroup);
    }

    public static void makeup(String qq, String reason, String askId) {
        if (reason == null)
            reason = "无";
        List<String[]> list = readShutLogs();
        int loc = -1;
        int time = 0;
        int latest = 0;

        for (String[] line : list) {
            if (line[1].equals(qq)) {
                if (line[0].equals("禁言")) {
                    time++;
                    latest = list.indexOf(line);
                    if (line[3].equals("无"))
                        loc = list.indexOf(line);
                } else
                    time--;
            }
        }

        if (loc == -1 && time == 0) {
            sendP(askId, "该用户没有禁言记录");
            return;
        }

        if (reason.equals("无")) {
            String[] target = list.get(latest);
            sendG("·查询到用户 [" + qq + "] \n总共被禁言" + time + "次\n最近一次禁言时间：" + target[4] + "\n·处理者： " + askId, ManagerGroup);
        } else {
            String[] target = list.get(loc);
            sendG("·查询到用户 [" + qq + "] \n总共被禁言" + time + "次 最近一次禁言原因改为：" + reason + "\n时间：" + target[4] + "\n·处理者： "
                    + askId, ManagerGroup);
            list.set(loc, new String[] { target[0], target[1], target[2], reason, target[4], askId });
            writeShutLogs(list);
        }
    }

    public static int getShutTime(List<String[]> list, String qq) {
        int time = 0;
        for (String[] line : list) {
            if (line[1].equals(qq) && line[0].equals("禁言"))
                time++;
        }
        return time;
    }

    public static String formatTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        return now.format(formatter);
    }
}
