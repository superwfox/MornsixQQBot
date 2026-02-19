package sudark2.Sudark.mornsixQQBot.onebot;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bukkit.plugin.Plugin;

import java.nio.file.Files;
import java.util.Base64;

import static sudark2.Sudark.mornsixQQBot.CommandHandler.formatTime;
import static sudark2.Sudark.mornsixQQBot.FileManager.ManagerGroup;
import static sudark2.Sudark.mornsixQQBot.FileManager.QQGroup;
import static sudark2.Sudark.mornsixQQBot.FileManager.shutLogs;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.client;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class OneBotApi {
    public static void refuseIn(String flag) {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_add_request");
        inner.put("flag", flag);
        inner.put("approve", "false");
        inner.put("reason", "您处于该群黑名单，可能是为了避免禁言退群，不予受理");
        json.put("params", inner);
        sendAction(json, "§7处理入群申请失败");
    }

    public static void setB() {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_whole_ban");
        inner.put("group_id", QQGroup);
        inner.put("enable", "true");
        json.put("params", inner);
        sendAction(json, "§7开启全体禁言失败");
    }

    public static void cancelB() {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_whole_ban");
        inner.put("group_id", QQGroup);
        inner.put("enable", "false");
        json.put("params", inner);
        sendAction(json, "§7关闭全体禁言失败");
    }

    public static void sendG(String message, String groupId) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("group_id", groupId);
        connectedi.put("message", message);
        connectedi.put("auto_escape", "false");
        connected.put("action", "send_group_msg");
        connected.put("params", connectedi);
        sendAction(connected, "§7发送群消息失败");
    }

    public static void transMsg(JSONArray array, String userId, String name, String reason, String groupId) {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        JSONObject data2 = new JSONObject();
        JSONObject type2 = new JSONObject();
        JSONObject typeo = new JSONObject();
        JSONObject typeo2 = new JSONObject();
        JSONObject datai = new JSONObject();
        JSONObject datai2 = new JSONObject();
        JSONArray msg = new JSONArray();
        JSONArray contents2 = new JSONArray();

        datai.put("name", name);
        datai.put("uin", userId);
        datai.put("content", array);
        typeo.put("type", "node");
        typeo.put("data", datai);

        data2.put("text", reason + "\n" + formatTime() + "\n" + name + "[" + userId + "]");
        type2.put("type", "text");
        type2.put("data", data2);
        contents2.add(type2);
        datai2.put("name", "kami");
        datai2.put("uin", "3364200181");
        datai2.put("content", contents2);
        typeo2.put("type", "node");
        typeo2.put("data", datai2);

        msg.add(typeo2);
        msg.add(typeo);
        inner.put("messages", msg);
        inner.put("group_id", groupId);
        json.put("action", "send_group_forward_msg");
        json.put("params", inner);
        sendAction(json, "§7发送转发消息失败");
    }

    public static void sendP(String user, String message) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", user);
        connectedi.put("message", message);
        connectedi.put("auto_escape", "false");
        connected.put("action", "send_private_msg");
        connected.put("params", connectedi);
        sendAction(connected, "§7发送私聊失败");
    }

    public static void ban(String user, Integer time, String groupId) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", user);
        connectedi.put("group_id", groupId);
        connectedi.put("duration", time);
        connectedi.put("auto_escape", "false");
        connected.put("action", "set_group_ban");
        connected.put("params", connectedi);
        sendAction(connected, "§7设置禁言失败");
    }

    public static void kickG(String userId) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", userId);
        connectedi.put("group_id", QQGroup);
        connectedi.put("reject_add_request", "true");
        connected.put("action", "set_group_kick");
        connected.put("params", connectedi);
        sendAction(connected, "§7踢人失败");
    }

    public static void uploadFileG() {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        String base64;
        try {
            byte[] fileContent = Files.readAllBytes(shutLogs.toPath());
            base64 = Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            warn("§7读取禁言日志失败");
            return;
        }

        connectedi.put("name", formatTime().replace(":", "-") + "违禁日志.csv");
        connectedi.put("group_id", ManagerGroup);
        connectedi.put("file", "base64://" + base64);
        connected.put("action", "upload_group_file");
        connected.put("params", connectedi);
        sendAction(connected, "§7上传日志失败");
    }

    public static void broadNotice(String content) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("content", content);
        connectedi.put("group_id", QQGroup);
        connected.put("action", "_send_group_notice");
        connected.put("params", connectedi);
        sendAction(connected, "§7发送群公告失败");
    }

    private static void sendAction(JSONObject payload, String warnMsg) {
        try {
            client.send(payload.toString());
        } catch (Exception e) {
            warn(warnMsg);
        }
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null) plugin.getLogger().warning(msg);
    }
}
