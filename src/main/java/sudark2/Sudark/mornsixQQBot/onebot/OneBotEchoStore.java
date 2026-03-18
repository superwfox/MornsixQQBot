package sudark2.Sudark.mornsixQQBot.onebot;

import it.unimi.dsi.fastutil.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.client;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class OneBotEchoStore {
    private static final ConcurrentHashMap<String, CompletableFuture<Pair<String, JSONArray>>> pending = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CompletableFuture<JSONObject>> filePending = new ConcurrentHashMap<>();

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
            CompletableFuture<Pair<String, JSONArray>> failFuture = pending.remove(msgId);
            if (failFuture != null && !failFuture.isDone()) failFuture.completeExceptionally(e);
            warn("§7获取消息失败");
        }
        return future;
    }

    public static CompletableFuture<JSONObject> getFile(String fileId) {
        String echoKey = "get_file_" + fileId;
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        filePending.put(echoKey, future);

        JSONObject json = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("file_id", fileId);
        json.put("action", "get_file");
        json.put("params", params);
        json.put("echo", echoKey);

        try {
            client.send(json.toString());
        } catch (Exception e) {
            CompletableFuture<JSONObject> failFuture = filePending.remove(echoKey);
            if (failFuture != null && !failFuture.isDone()) failFuture.completeExceptionally(e);
            warn("§7获取文件失败");
        }
        return future;
    }

    public static void handleEcho(JSONObject json) {
        String echo = json.optString("echo", null);
        if (echo == null) return;

        // Handle file responses
        CompletableFuture<JSONObject> fileFuture = filePending.remove(echo);
        if (fileFuture != null && !fileFuture.isDone()) {
            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                fileFuture.completeExceptionally(new IllegalStateException("missing data"));
            } else {
                fileFuture.complete(data);
            }
            return;
        }

        CompletableFuture<Pair<String, JSONArray>> future = pending.remove(echo);
        if (future == null || future.isDone()) return;

        JSONObject data = json.optJSONObject("data");
        if (data == null) {
            future.completeExceptionally(new IllegalStateException("missing data"));
            return;
        }

        String userId = data.optString("user_id", String.valueOf(data.optLong("user_id", -1)));
        JSONArray msg = data.optJSONArray("message");
        if (msg == null) {
            String rawMessage = data.optString("raw_message", data.optString("message", null));
            if (rawMessage == null || rawMessage.isEmpty()) {
                future.completeExceptionally(new IllegalStateException("missing message"));
                return;
            }
            JSONObject textData = new JSONObject();
            textData.put("text", rawMessage);
            JSONObject textSeg = new JSONObject();
            textSeg.put("type", "text");
            textSeg.put("data", textData);
            msg = new JSONArray();
            msg.add(textSeg);
        }
        future.complete(Pair.of(userId, msg));
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null) plugin.getLogger().warning(msg);
    }
}
