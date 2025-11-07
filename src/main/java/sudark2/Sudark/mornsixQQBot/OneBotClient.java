package sudark2.Sudark.mornsixQQBot;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.regexp.RegexpMatcher;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static sudark2.Sudark.mornsixQQBot.CommandHandler.*;
import static sudark2.Sudark.mornsixQQBot.FileManager.*;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.ServerURI;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.client;

public class OneBotClient extends WebSocketClient {
    private static final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public OneBotClient() {
        super(ServerURI);
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

    public static void transMsg(String msgId, String QQGroup) {
        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("message_id", msgId);
        connectedi.put("group_id", QQGroup);
        connected.put("action", "forward_group_single_msg");
        connected.put("params", connectedi);
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

    public static CompletableFuture<String> checkUser(String msgId) {
        String echo = "getMsg" + msgId;
        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(echo, future);

        JSONObject connected = new JSONObject();
        JSONObject connectedi = new JSONObject();
        connectedi.put("message_id", msgId);
        connected.put("action", "get_msg");
        connected.put("params", connectedi);
        connected.put("echo", echo);

        try {
            client.send(connected.toString());
        } catch (Exception e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }

        return future; // 返回 future 给调用方
    }

    public static void handleMessage(String msg) {
        JSONObject json = JSONObject.fromObject(msg);
        String echo = json.optString("echo", null);
        if (echo != null && pending.containsKey(echo)) {
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                String userId = String.valueOf(data.optLong("user_id", -1));
                pending.remove(echo).complete(userId); // 完成 future
            }
        }
    }


    @Override
    public void onMessage(String s) {
        JSONObject json = JSONObject.fromObject(s);

        if (!json.containsKey("message_type")) return;
        String type = json.getString("message_type");

        JSONObject sender = json.getJSONObject("sender");
        String userId = sender.getString("user_id");

        if (type.equals("group")) {
            String groupId = json.getString("group_id");

            if (groupId.equals(QQGroup)) {
                if (users.contains(userId)) {
                    banOrKick(json);
                    return;
                }
                String msg = json.getString("raw_message");
                boolean matched = Regex.stream()
                        .anyMatch(r -> Pattern.compile(r).matcher(msg).find());

                if (matched) {
                    transMsg(json.getString("message_id"), ManagerGroup);
                    sendG("检测到违规消息 " + userId, ManagerGroup);
                }
            }

            if (groupId.equals(ManagerGroup)) {

            }

        } else {

            if (!users.contains(userId)) return;

            String msg = json.getString("raw_message");

            if (!msg.startsWith("/")) return;
            String[] args = msg.substring(1).split(" ");

            switch (args[0]) {
                case "curfew" -> curfew(args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
                case "prefix" -> regex(args[1], args[2]);
                case "file" -> file();

            }
        }


    }

    public static void banOrKick(JSONObject json) {
        JSONArray msgs = json.getJSONArray("message");
        if (msgs.getJSONObject(0).getString("type").equals("reply")) {
            String msgId = json.getJSONArray("message").getJSONObject(0).getJSONObject("data").getString("id");

            checkUser(msgId).thenAccept(checkedUserId -> {
                if (msgs.getJSONObject(1).getString("type").equals("text")) {

                    String msg = msgs.getJSONObject(1).getJSONObject("data").getString("text");
                    String[] args = msg.split(" ");
                    switch (args[0]) {
                        case "shut" -> shut(checkedUserId, Integer.valueOf(args[1]), args[2]);
                        case "unshut" -> unshut(args[1]);

                    }
                }
            });

        }
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
