package sudark2.Sudark.mornsixQQBot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.*;

public class CommandHandler {

    public static void curfew(String mode, Integer hours, Integer minutes) {
        if (mode.equals("on")) {
            if (hours == null) hours = 0;
            if (minutes == null) minutes = 0;

            curfewTime[0] = hours;
            curfewTime[1] = minutes;
            writeCurfew(curfewTime);
            sendG("宵禁开始时间为" + hours + ":" + minutes, ManagerGroup);
        }

        if (mode.equals("off")) {
            if (hours == null) hours = 0;
            if (minutes == null) minutes = 0;

            curfewTime[2] = hours;
            curfewTime[3] = minutes;
            writeCurfew(curfewTime);
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
            if (!Regex.contains(regex)) {
                sendG("未找到该正则", ManagerGroup);
                return;
            }
            Regex.remove(regex);
            writeRegexList();
            sendG("已删除正则\n " + regex, ManagerGroup);
        }
        if (mode.equals("list")) {
            String list = String.join("\n", Regex);
            sendG(" 正则列表 :\n" + list, ManagerGroup);
        }
    }

    public static int getShutTime(List<String[]> list, String qq) {
        int time = 0;

        for (String[] line : list) {
            if (line[1].equals(qq)) {
                if (line[0].equals("禁言")) {
                    time++;
                }
            }
        }

        return time;
    }

    public static void shut(String userId, Integer time, String reason, String askId) {
        if (time == null) time = 0;
        if (reason == null) reason = "无";
        List<String[]> args = readShutLogs();
        args.add(new String[]{"禁言", userId, time.toString(), reason, formatTime(), askId});
        int banTime = getShutTime(args, userId);
        writeShutLogs(args);

        ban(userId, time, QQGroup);
        sendG("已禁言 [" + userId + "] " + time + "秒\n原因：" + reason + "\n处理者： " + askId + "\n总禁言次数 ： " + banTime, ManagerGroup);
        sendG("已禁言 [" + userId + "] " + time + "秒\n原因：" + reason + "\n总禁言次数 ： " + banTime, QQGroup);
    }

    public static void shutAuto(String userId, String time, String askId) {
        String reason = "[回复此消息补充]";
        List<String[]> args = readShutLogs();
        args.add(new String[]{"禁言", userId, time, "无", formatTime(), askId});
        int banTime = getShutTime(args, userId);
        writeShutLogs(args);
        sendG("已禁言 [" + userId + "] " + time + "秒\n原因：" + reason + "\n处理者： " + askId + "\n总禁言次数 ： " + banTime, ManagerGroup);
    }

    public static void unShutAuto(String userId, String askId) {
        List<String[]> args = readShutLogs();
        args.add(new String[]{"解禁", userId, "0", "无", formatTime(), askId});
        writeShutLogs(args);
        sendG("已解禁 [" + userId + "]\n处理者： " + askId, ManagerGroup);
    }

    public static void unshut(String userId, String askId) {
        List<String[]> args = readShutLogs();
        args.add(new String[]{"解禁", userId, "0", "无", formatTime(), askId});
        writeShutLogs(args);
        ban(userId, 0, QQGroup);
        sendG("已解禁 [" + userId + "]\n处理者： " + askId, ManagerGroup);
        sendG("已解禁 [" + userId + "]", QQGroup);
    }

    public static void file() {
        uploadFileG();
    }

    public static void admin(String mode, String userId, String askId) {
        if (mode.equals("add")) {
            if (users.contains(userId)) {
                sendP(askId, "该用户已经是管理员");
            } else {
                users.add(userId);
                writeSuperUsers(users);
                sendG("已添加管理员\n" + userId, ManagerGroup);
            }
        } else {
            if (users.contains(userId)) {
                users.remove(userId);
                writeSuperUsers(users);
                sendG("已删除管理员\n" + userId, ManagerGroup);
            } else {
                sendP(askId, "该用户不是管理员");
            }
        }
    }

    public static void kick(String userId, String reason, String askId) {
        if (reason == null) reason = "无";

        List<String[]> args = readShutLogs();
        args.add(new String[]{"踢出", userId, "0", reason, formatTime(), askId});
        writeShutLogs(args);

        kickG(userId);
        sendG("已踢出" + userId + "\n原因：" + reason + "\n处理者： " + askId, ManagerGroup);
    }

    public static void setGroup(String g1, String g2, String askId) {
        if (g1 == null) g1 = "0";
        if (g2 == null) g2 = "0";
        sendP(askId, "已设置群号为" + g1 + " " + g2);
        QQGroup = g1;
        ManagerGroup = g2;
        writeGroupList(new String[]{g1, g2});
    }

    public static void setNotice(String content, String askId) {
        sendP(askId, "已设置公告为\n" + content);
        writeNotice(content);
    }

    public static void setMice(String mode, String qq, String askId) {
        if (mode.equals("add")) {
            mice.add(qq);
            writeMice(mice);
            sendP(askId, "已添加黑名单\n" + qq);
        } else {
            if (mice.contains(qq)) {
                mice.remove(qq);
                writeMice(mice);
                sendP(askId, "已从黑名单移除\n" + qq);
            } else sendP(askId, "该QQ不在黑名单中");
        }
    }

    public static void replyMakeup(String qq, String reason, String askId) {
        List<String[]> list = readShutLogs();
        int latest = 0;
        int loc = -1;

        for (String[] line : list) {
            if (line[1].equals(qq)) {
                if (line[0].equals("禁言")) {
                    latest = list.indexOf(line);
                    loc = list.indexOf(line);
                }
            }
        }

        if (loc == -1) {
            sendP(askId, "该用户没有禁言记录");
            return;
        }

        String[] target = list.get(latest);
        list.set(loc, new String[]{target[0], target[1], target[2], reason, target[4], askId});
        writeShutLogs(list);

        sendG("·将 [" + qq + "] \n" + target[4] + "\n\n禁言原因修改为: " + reason + "\n·处理者： " + askId, ManagerGroup);

    }

    public static void makeup(String qq, String reason, String askId) {
        if (reason == null) reason = "无";
        List<String[]> list = readShutLogs();
        int loc = -1;
        int time = 0;
        int latest = 0;

        for (String[] line : list) {
            if (line[1].equals(qq)) {
                if (line[0].equals("禁言")) {
                    time++;
                    latest = list.indexOf(line);
                    if (line[3].equals("无")) {
                        loc = list.indexOf(line);
                    }
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
            sendG("·查询到用户 [" + qq + "] \n总共被禁言" + time + "次 最近一次禁言原因改为：" + reason + "\n时间：" + target[4] + "\n·处理者： " + askId, ManagerGroup);
            list.set(loc, new String[]{target[0], target[1], target[2], reason, target[4], askId});
            writeShutLogs(list);
        }
    }

    public static String formatTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        return now.format(formatter);
    }


}
