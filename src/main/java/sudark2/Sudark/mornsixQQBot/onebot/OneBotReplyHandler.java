package sudark2.Sudark.mornsixQQBot.onebot;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sudark2.Sudark.mornsixQQBot.CommandHandler.kick;
import static sudark2.Sudark.mornsixQQBot.CommandHandler.replyMakeup;
import static sudark2.Sudark.mornsixQQBot.CommandHandler.shut;
import static sudark2.Sudark.mornsixQQBot.CommandHandler.unshut;
import static sudark2.Sudark.mornsixQQBot.FileManager.BotQQ;
import static sudark2.Sudark.mornsixQQBot.FileManager.ManagerGroup;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendP;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotEchoStore.checkUser;

public class OneBotReplyHandler {
    private static final Pattern SHUT_MESSAGE_HEADER_PATTERN = Pattern.compile("^已禁言\\s*\\[(\\d+)]\\s*\\d+秒$");
    private static final Pattern SHUT_MESSAGE_OPERATOR_PATTERN = Pattern.compile("^处理者：\\s*\\d+$");
    private static final Pattern SHUT_MESSAGE_COUNT_PATTERN = Pattern.compile("^总禁言次数\\s*：\\s*\\d+$");
    private static final String SHUT_REASON_PLACEHOLDER = "原因：[回复此消息补充]";

    public static void banOrKick(JSONObject json, String askId, String groupId) {
        JSONArray msgs = json.getJSONArray("message");
        if (msgs == null || msgs.isEmpty()) return;
        if (!"reply".equals(msgs.getJSONObject(0).getString("type"))) return;

        String msgId = msgs.getJSONObject(0).getJSONObject("data").getString("id");
        String commandMsg = getMsgInReply(msgs);

        checkUser(msgId).thenAccept(back -> {
            String id = back.left();
            JSONArray msgArray = back.right();

            if (BotQQ.equals(id)) {
                handleReplyMakeup(askId, groupId, msgArray, commandMsg);
                return;
            }

            if (commandMsg == null || commandMsg.isBlank()) return;
            String[] args = commandMsg.trim().split("\\s+");
            try {
                switch (args[0]) {
                    case "ban" -> shut(id, Integer.valueOf(args[1]), args[2], askId);
                    case "unban" -> unshut(id, askId);
                    case "kick" -> kick(id, args[1], askId);
                }
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                sendP(askId, "§7回复中的命令参数不足或格式错误");
            }
        }).exceptionally(ex -> {
            sendP(askId, "§7获取被回复消息失败，请稍后重试");
            return null;
        });
    }

    private static void handleReplyMakeup(String askId, String groupId, JSONArray msgArray, String commandMsg) {
        if (!ManagerGroup.equals(groupId)) {
            sendP(askId, "§7禁言备注补充仅支持在管理群回复");
            return;
        }

        String targetId = extractTargetIdFromShutMessage(msgArray);
        if (targetId == null) {
            sendP(askId, "§7回复目标不是禁言补充消息，或消息格式不匹配");
            return;
        }
        if (commandMsg == null || commandMsg.isBlank()) {
            sendP(askId, "§7你的回复中缺少文本理由");
            return;
        }

        replyMakeup(targetId, commandMsg.trim(), askId);
    }

    private static String getMsgInReply(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JSONObject seg = arr.getJSONObject(i);
            if ("text".equals(seg.optString("type"))) return seg.getJSONObject("data").getString("text");
        }
        return null;
    }

    private static String extractTargetIdFromShutMessage(JSONArray msgArray) {
        String msg = getAllText(msgArray);
        if (msg == null) return null;

        String normalized = msg.replace("\r\n", "\n").trim();
        String[] lines = normalized.split("\n");
        if (lines.length < 4) return null;

        Matcher headerMatcher = SHUT_MESSAGE_HEADER_PATTERN.matcher(lines[0].trim());
        if (!headerMatcher.matches()) return null;
        if (!SHUT_REASON_PLACEHOLDER.equals(lines[1].trim())) return null;
        if (!SHUT_MESSAGE_OPERATOR_PATTERN.matcher(lines[2].trim()).matches()) return null;
        if (!SHUT_MESSAGE_COUNT_PATTERN.matcher(lines[3].trim()).matches()) return null;
        return headerMatcher.group(1);
    }

    private static String getAllText(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject seg = arr.getJSONObject(i);
            if ("text".equals(seg.optString("type"))) {
                sb.append(seg.getJSONObject("data").optString("text", ""));
            }
        }
        String result = sb.toString();
        return result.isBlank() ? null : result;
    }
}
