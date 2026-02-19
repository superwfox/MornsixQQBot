package sudark2.Sudark.mornsixQQBot;

import it.unimi.dsi.fastutil.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static sudark2.Sudark.mornsixQQBot.CommandHandler.*;
import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.ServerURI;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.client;

public class OneBotClient extends WebSocketClient {
    private static final ConcurrentHashMap<String, CompletableFuture<Pair<String, JSONArray>>> pending = new ConcurrentHashMap<>();

    public OneBotClient() {
        super(ServerURI);
    }

    public static void refuseIn(String flag) {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_add_request");
        inner.put("flag", flag);
        inner.put("approve", "false");
        inner.put("reason", "您处于该群黑名单，可能是为了避免禁言退群，不予受理");
        json.put("params", inner);

        try {
            client.send(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setB() {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_whole_ban");
        inner.put("group_id", QQGroup);
        inner.put("enable", "true");
        json.put("params", inner);

        try {
            client.send(json.toString());
        } catch (Exception var4) {
            Exception e = var4;
            e.printStackTrace();
        }

    }

    public static void cancelB() {
        JSONObject json = new JSONObject();
        JSONObject inner = new JSONObject();
        json.put("action", "set_group_whole_ban");
        inner.put("group_id", QQGroup);
        inner.put("enable", "false");
        json.put("params", inner);

        try {
            client.send(json.toString());
        } catch (Exception var4) {
            Exception e = var4;
            e.printStackTrace();
        }

    }

    public static void sendG(String message, String QQGroup) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("group_id", QQGroup);
        connectedi.put("message", message);
        connectedi.put("auto_escape", "false");
        connected.put("action", "send_group_msg");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var5) {
            Exception e = var5;
            e.printStackTrace();
        }

    }

    public static void transMsg(JSONArray array, String userId, String name, String reason, String QQGroup) {
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
        inner.put("group_id", QQGroup);
        json.put("action", "send_group_forward_msg");
        json.put("params", inner);

        try {
            client.send(json.toString());
        } catch (Exception var17) {
            Exception e = var17;
            e.printStackTrace();
        }

    }

    public static void sendP(String user, String message) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", user);
        connectedi.put("message", message);
        connectedi.put("auto_escape", "false");
        connected.put("action", "send_private_msg");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }

    }

    public static void ban(String user, Integer time, String QQGroup) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", user);
        connectedi.put("group_id", QQGroup);
        connectedi.put("duration", time);
        connectedi.put("auto_escape", "false");
        connected.put("action", "set_group_ban");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }

    }

    public static void kickG(String userId) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("user_id", userId);
        connectedi.put("group_id", QQGroup);
        connectedi.put("reject_add_request", "true");
        connected.put("action", "set_group_kick");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }

    }

    public static void uploadFileG() {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        String base64 = "";
        try {
            byte[] fileContent = Files.readAllBytes(shutLogs.toPath());
            base64 = Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectedi.put("name", formatTime().replace(":", "-") + "违禁日志.csv");
        connectedi.put("group_id", ManagerGroup);
        connectedi.put("file", "base64://" + base64);
        connected.put("action", "upload_group_file");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }
    }

    public static void broadNotice(String content) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("content", content);
        connectedi.put("group_id", QQGroup);
        connected.put("action", "_send_group_notice");
        connected.put("params", connectedi);

        try {
            client.send(connected.toString());
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }
    }

//    public static void deleteMsg(String msgId) {
//        JSONObject connected = new JSONObject();
//        JSONObject connectedi = new JSONObject();
//        connectedi.put("message_id", msgId);
//        connected.put("action", "delete_msg");
//        connected.put("params", connectedi);
//
//        try {
//            client.send(connected.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static CompletableFuture<Pair<String, JSONArray>> checkUser(String msgId) {
        CompletableFuture<Pair<String, JSONArray>> future = new CompletableFuture<>();
        pending.put(msgId, future);

        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("message_id", msgId);
        connected.put("action", "get_msg");
        connected.put("params", connectedi);
        connected.put("echo", msgId);

        try {
            client.send(connected.toString());
        } catch (Exception e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }

        return future; // 返回 future 给调用方
    }

    public static void handleMessage(JSONObject json) {
        String echo = json.optString("echo", null);
        if (echo == null) return;

        JSONObject data = json.optJSONObject("data");
        String userId = String.valueOf(data.optLong("user_id", -1));
        JSONArray msg = data.getJSONArray("message");
        pending.remove(echo).complete(Pair.of(userId, msg)); // 完成 future
    }

    public static String translateMsg(JSONObject json) {
        //确定消息内容 msg
        StringBuilder MB = new StringBuilder();
        JSONArray message = json.getJSONArray("message");
        for (int i = 0; i < message.size(); i++) {
            JSONObject obj = message.getJSONObject(i);
            String type = obj.optString("type");
            switch (type) {
                case "text":
                    MB.append(obj.getJSONObject("data").getString("text"));
                    break;
                case "face":
                    MB.append("[§b表情§f]");
                    break;
                case "image":
                    MB.append("[§b图片§f]");
                    break;
                case "at":
                    String nickname = obj.getJSONObject("data").getString("name");
                    MB.append("[§b@" + nickname + "§f]");
                    break;
                case "reply":
                    MB.append("[§b回复§f]");
                    break;
                case "video":
                    MB.append("[§b视频§f]");
                    break;
                default:
                    MB.append("[§b未知消息§f]");
            }
        }
        return MB.toString();
    }


    @Override
    public void onMessage(String s) {
        JSONObject json = JSONObject.fromObject(s);

        handleMessage(json);

        if (json.containsKey("request_type") && json.getString("request_type").equals("group") && json.getString("group_id").equals(QQGroup)) {
            String userId = json.getString("user_id");
            String flag = json.getString("flag");
            if (mice.contains(userId)) {
                refuseIn(flag);
                sendG(userId + "申请加群 \n位于黑名单自动拒绝", ManagerGroup);
            }
            return;
        }

        if (json.containsKey("message_type")) {
            String type = json.getString("message_type");

            JSONObject sender = json.getJSONObject("sender");
            String userId = sender.getString("user_id");
            String nickname = sender.getString("nickname");

            if (userId.equals("2854196310")) return;

            if (type.equals("group")) {
                String groupId = json.getString("group_id");

                if (groupId.equals(QQGroup)) {

                    if (users.contains(userId)) {
                        banOrKick(json, userId);
                        return;
                    }

                    String msg = translateMsg(json);
                    boolean matched = Regex.stream()
                            .map(Pattern::compile)      // 先把每个正则编译成 Pattern 对象
                            .anyMatch(p -> p.matcher(msg).find());

                    if (matched) {
                        transMsg(json.getJSONArray("message"), userId, nickname, "消息违规！", ManagerGroup);
                    }

                }

                if (groupId.equals(ManagerGroup)) {
                }

            } else {

                String msg = json.getString("raw_message");
                if (msg.equals("/update")) {
                    initFiles();
                    sendP(userId, "已重载配置文件");
                    return;
                }

                if (!users.contains(userId)) return;

                if (!msg.startsWith("/")) return;
                String[] args = msg.substring(1).split(" ");

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
                }

            }
            return;
        }

        if (json.containsKey("notice_type") && json.getString("group_id").equals(QQGroup)) {

            String userId = json.getString("user_id");
            if (userId.equals("0")) return;

            switch (json.getString("notice_type")) {
                case "group_ban" -> {
                    String askId = json.getString("operator_id");
                    String duration = json.getString("duration");
                    String type = json.getString("sub_type");
                    if (type.equals("ban")) {
                        shutAuto(userId, duration, askId);
                    } else {
                        unShutAuto(userId, askId);
                    }
                }
                case "group_recall" -> {
                    String msgId = json.getString("message_id");

                    checkUser(msgId).thenAccept(msg -> {
                        JSONArray msgArray = msg.right();
                        transMsg(msgArray, userId, "", "群内撤回", "1064467046");
                    });
                }

            }

        }


    }

    public static void banOrKick(JSONObject json, String askId) {
        JSONArray msgs = json.getJSONArray("message");
        if (msgs.getJSONObject(0).getString("type").equals("reply")) {
            String msgId = json.getJSONArray("message").getJSONObject(0).getJSONObject("data").getString("id");

            checkUser(msgId).thenAccept(back -> {
                String id = back.left();
                JSONArray msgArray = back.right();
                String msg = getMsgInReply(msgArray);

                if (id.equals(BotQQ)) {
                    String targetMsg = msgArray.getJSONObject(0).getJSONObject("data").getString("text");
                    String preQQ = targetMsg.split(" ")[1];
                    String targetId = preQQ.substring(1, preQQ.length() - 1);

                    replyMakeup(targetId, msg, askId);
                }

                if (msgs.getJSONObject(1).getString("type").equals("text")) {
                    String[] args = msg.split(" ");
                    switch (args[0]) {
                        case "ban" -> shut(id, Integer.valueOf(args[1]), args[2], askId);
                        case "unban" -> unshut(id, askId);

                        case "kick" -> kick(id, args[1], askId);
                    }
                }
            });

        }
    }

    private static String getMsgInReply(JSONArray arr) {
        int startIndex = 1;
        for (int i = 0; i < arr.size(); i++) {
            if (arr.getJSONObject(startIndex).get("type").equals("text"))
                return arr.getJSONObject(startIndex).getJSONObject("data").getString("text");
        }
        return null;
    }


    @Override
    public void onClose(int i, String s, boolean b) {
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }
}
