package sudark2.Sudark.mornsixQQBot.command;

import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.*;

public class AdminCommands {

    public static void curfew(String mode, Integer hours, Integer minutes) {
        if (mode.equals("on")) {
            if (hours == null)
                hours = 0;
            if (minutes == null)
                minutes = 0;
            curfewTime[0] = hours;
            curfewTime[1] = minutes;
            writeCurfew(curfewTime);
            sendG("宵禁开始时间为" + hours + ":" + minutes, ManagerGroup);
        }
        if (mode.equals("off")) {
            if (hours == null)
                hours = 0;
            if (minutes == null)
                minutes = 0;
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

    public static void setGroup(String g1, String g2, String askId) {
        if (g1 == null)
            g1 = "0";
        if (g2 == null)
            g2 = "0";
        sendP(askId, "已设置群号为" + g1 + " " + g2);
        QQGroup = g1;
        ManagerGroup = g2;
        writeGroupList(new String[] { g1, g2 });
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
            } else
                sendP(askId, "该QQ不在黑名单中");
        }
    }

    public static void file() {
        uploadFileG();
    }

    public static void addUid(String uid, String askId) {
        if (biliUids.contains(uid)) {
            sendP(askId, "该UID已在监控列表中");
            return;
        }
        biliUids.add(uid);
        writeBiliUids();
        sendP(askId, "已添加B站监控\n" + uid);
    }

    public static void removeUid(String uid, String askId) {
        if (!biliUids.contains(uid)) {
            sendP(askId, "该UID不在监控列表中");
            return;
        }
        biliUids.remove(uid);
        writeBiliUids();
        sendP(askId, "已移除B站监控\n" + uid);
    }

    public static void checkUid(String askId) {
        if (biliUids.isEmpty()) {
            sendP(askId, "监控列表为空");
            return;
        }
        sendP(askId, "B站监控列表:\n" + String.join("\n", biliUids));
    }

    public static void setEmail(String[] args, String askId) {
        if (args.length < 3) {
            sendP(askId, "命令格式错误\n正确格式: /setEmail 邮箱地址 应用密码\n示例: /setEmail user@outlook.com your_app_password\n可选: /setEmail 邮箱 密码 imapHost imapPort smtpHost smtpPort");
            return;
        }

        String email = args[1];
        String password = args[2];

        if (args.length >= 7) {
            sudark2.Sudark.mornsixQQBot.EmailRelated.EmailConfig.saveConfig(
                    email, password, args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]));
        } else {
            sudark2.Sudark.mornsixQQBot.EmailRelated.EmailConfig.saveConfig(email, password);
        }

        if (!sudark2.Sudark.mornsixQQBot.EmailRelated.ImapSmtpClient.testConnection()) {
            sendP(askId, "邮箱登录失败\n请检查应用密码是否正确");
            return;
        }

        String maskedPassword = sudark2.Sudark.mornsixQQBot.EmailRelated.EmailConfig.maskPassword(password);
        sendP(askId, "已设置邮箱配置\n邮箱: " + email + "\n密码: " + maskedPassword);
        sendG("邮箱监控已启用\n邮箱: " + email, ManagerGroup);
    }
}
