package sudark2.Sudark.mornsixQQBot.onebot;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.regex.Pattern;

import static sudark2.Sudark.mornsixQQBot.command.AdminCommands.*;
import static sudark2.Sudark.mornsixQQBot.command.BanCommands.*;
import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotEchoStore.checkUser;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotEchoStore.getFile;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.refuseIn;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendG;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendP;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.transMsg;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotReplyHandler.banOrKick;

public class OneBotEventRouter {

    public static void handle(JSONObject json) {
        if (handleGroupRequest(json))
            return;
        if (handleChatMessage(json))
            return;
        handleNotice(json);
    }

    private static boolean handleGroupRequest(JSONObject json) {
        if (!json.containsKey("request_type"))
            return false;
        if (!"group".equals(json.optString("request_type", "")))
            return false;
        if (!QQGroup.equals(json.optString("group_id", "")))
            return false;

        String userId = json.getString("user_id");
        if (!mice.contains(userId))
            return true;

        String flag = json.getString("flag");
        refuseIn(flag);
        sendG(userId + "申请加群 \n位于黑名单自动拒绝", ManagerGroup);
        return true;
    }

    private static boolean handleChatMessage(JSONObject json) {
        if (!json.containsKey("message_type"))
            return false;

        JSONObject sender = json.getJSONObject("sender");
        String userId = sender.getString("user_id");
        String nickname = sender.getString("nickname");
        if ("2854196310".equals(userId))
            return true;

        String type = json.getString("message_type");
        if ("group".equals(type)) {
            handleGroupMessage(json, userId, nickname);
            return true;
        }

        handlePrivateMessage(json, userId);
        return true;
    }

    private static void handleGroupMessage(JSONObject json, String userId, String nickname) {
        String groupId = json.getString("group_id");
        if (!groupId.equals(QQGroup) && !groupId.equals(ManagerGroup))
            return;

        if (users.contains(userId)) {
            banOrKick(json, userId, groupId);
            return;
        }

        if (!groupId.equals(QQGroup))
            return;
        String msg = translateMsg(json);
        boolean matched = Regex.stream().map(Pattern::compile).anyMatch(p -> p.matcher(msg).find());
        if (matched)
            transMsg(json.getJSONArray("message"), userId, nickname, "消息违规！", ManagerGroup);
    }

    private static void handlePrivateMessage(JSONObject json, String userId) {
        String msg = json.getString("raw_message");
        if ("/update".equals(msg)) {
            initFiles();
            sendP(userId, "已重载配置文件");
            return;
        }

        if (!users.contains(userId))
            return;
        if (!msg.startsWith("/"))
            return;

        String[] args = msg.substring(1).split(" ");
        try {
            switch (args[0].toLowerCase()) {
                case "curfew" -> curfew(args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
                case "regex" -> regex(args[1], args.length > 2 ? args[2] : null);
                case "file" -> file();
                case "admin" -> admin(args[1], args[2], userId);
                case "kick" -> kick(args[1], args.length > 2 ? args[2] : null, userId);
                case "ban" -> shut(args[1], Integer.valueOf(args[2]), args.length > 3 ? args[3] : null, userId);
                case "unban" -> unshut(args[1], userId);
                case "setgroup" -> setGroup(args[1], args[2], userId);
                case "setnotice" -> setNotice(msg.substring(10), userId);
                case "setmice" -> setMice(args[1], args[2], userId);
                case "makeup" -> makeup(args[1], args.length > 2 ? args[2] : null, userId);
                case "adduid" -> addUid(args[1], userId);
                case "removeuid" -> removeUid(args[1], userId);
                case "checkuid" -> checkUid(userId);
                case "testuid" -> sudark2.Sudark.mornsixQQBot.BiliDataSniffer.BiliChecker.testFirst(userId);
                case "setemail" -> setEmail(args, userId);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            sendP(userId, "§7参数不足或格式错误");
        }
    }

    private static void handleNotice(JSONObject json) {
        if (!json.containsKey("notice_type"))
            return;

        String noticeType = json.getString("notice_type");
        String groupId = json.optString("group_id", "");

        if ("group_upload".equals(noticeType)) {
            handleGroupUpload(json, groupId);
            return;
        }

        if (!QQGroup.equals(groupId))
            return;

        String userId = json.getString("user_id");
        if ("0".equals(userId))
            return;

        switch (noticeType) {
            case "group_ban" -> {
                String askId = json.getString("operator_id");
                String duration = json.getString("duration");
                String type = json.getString("sub_type");
                if ("ban".equals(type))
                    shutAuto(userId, duration, askId);
                else
                    unShutAuto(userId, askId);
            }
            case "group_recall" -> {
                String msgId = json.getString("message_id");
                checkUser(msgId).thenAccept(msg -> transMsg(msg.right(), userId, "", "群内撤回", MsgStoreGroup))
                        .exceptionally(ex -> {
                            sendP(userId, "§7获取撤回消息失败");
                            return null;
                        });
            }
        }
    }

    private static void handleGroupUpload(JSONObject json, String groupId) {
        if (!ManagerGroup.equals(groupId))
            return;
        String userId = json.optString("user_id", "");
        if (!users.contains(userId))
            return;

        JSONObject fileInfo = json.optJSONObject("file");
        if (fileInfo == null)
            return;
        String fileName = fileInfo.optString("name", "");
        if (!fileName.toLowerCase().endsWith(".csv"))
            return;

        String fileId = fileInfo.optString("id", "");
        if (fileId.isEmpty())
            return;

        getFile(fileId).thenAccept(data -> {
            try {
                byte[] content = null;
                String url = data.optString("url", "");
                if (!url.isEmpty()) {
                    try (InputStream in = URI.create(url).toURL().openStream()) {
                        content = in.readAllBytes();
                    }
                } else {
                    String base64 = data.optString("base64", "");
                    if (!base64.isEmpty()) {
                        content = Base64.getDecoder().decode(base64);
                    }
                }
                if (content != null) {
                    replaceShutLogs(content);
                    sendG("已替换 shutLogs.csv（由 " + userId + " 上传）", ManagerGroup);
                } else {
                    sendG("获取文件内容失败：无可用的下载方式", ManagerGroup);
                }
            } catch (Exception e) {
                sendG("替换 shutLogs.csv 失败：" + e.getMessage(), ManagerGroup);
            }
        }).exceptionally(ex -> {
            sendG("获取文件信息失败：" + ex.getMessage(), ManagerGroup);
            return null;
        });
    }

    private static String translateMsg(JSONObject json) {
        StringBuilder mb = new StringBuilder();
        JSONArray message = json.getJSONArray("message");
        for (int i = 0; i < message.size(); i++) {
            JSONObject obj = message.getJSONObject(i);
            String type = obj.optString("type");
            switch (type) {
                case "text" -> mb.append(obj.getJSONObject("data").getString("text"));
                case "face" -> mb.append("[§b表情§f]");
                case "image" -> mb.append("[§b图片§f]");
                case "at" -> {
                    String nickname = obj.getJSONObject("data").getString("name");
                    mb.append("[§b@").append(nickname).append("§f]");
                }
                case "reply" -> mb.append("[§b回复§f]");
                case "video" -> mb.append("[§b视频§f]");
                default -> mb.append("[§b未知消息§f]");
            }
        }
        return mb.toString();
    }
}
