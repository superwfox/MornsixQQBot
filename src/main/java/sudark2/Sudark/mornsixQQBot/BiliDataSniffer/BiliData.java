package sudark2.Sudark.mornsixQQBot.BiliDataSniffer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BiliData {
    private final Long publishTs;
    private final String userFace;
    private final String userId;
    private final String jumpUrl;
    private final JSONObject latestItem;

    public BiliData(Long publishTs, String userFace, String userId, String jumpUrl, JSONObject content) {
        this.publishTs = publishTs;
        this.userFace = userFace;
        this.userId = userId;
        this.jumpUrl = jumpUrl;
        this.latestItem = content;
    }

    public static BiliData getBiliData(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("§7UID 不能为空");
        }

        long hostMid;
        try {
            hostMid = Long.parseLong(uid.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("§7UID 必须是数字: " + uid, e);
        }

        try {
            JSONObject root = HttpsHandler.fetchSpace(hostMid);
            if (root == null) {
                throw new IllegalStateException("§7接口返回为空");
            }

            int code = root.optInt("code", -1);
            if (code != 0) {
                if (code == 412 || code == -412) {
                    throw new IllegalStateException(
                            "§7接口触发风控(" + code + ")，请刷新浏览器Cookie并调用 HttpsHandler.setCookie(...)");
                }
                throw new IllegalStateException("§7接口异常 code=" + code + ", message=" + root.optString("message", ""));
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null) {
                throw new IllegalStateException("§7接口缺少 data 字段");
            }

            JSONArray items = data.optJSONArray("items");
            if (items == null || items.isEmpty()) {
                throw new IllegalStateException("§7该用户没有可用动态");
            }

            JSONObject firstItem = items.getJSONObject(0);
            JSONObject modules = firstItem.optJSONObject("modules");
            if (modules == null) {
                throw new IllegalStateException("§7动态缺少 modules 字段");
            }

            JSONObject author = modules.optJSONObject("module_author");
            if (author == null) {
                throw new IllegalStateException("§7动态缺少 module_author 字段");
            }

            long publishTs = parseLongOrZero(author.optString("pub_ts", "0"));
            if (publishTs <= 0) {
                throw new IllegalStateException("§7动态缺少有效发布时间 pub_ts");
            }

            String userFace = author.optString("face", "");
            String userId = author.optString("name", "");
            if (userId.isBlank()) {
                throw new IllegalStateException("§7动态缺少用户名称 name");
            }
            String rawJump = author.optString("jump_url", "");
            String jumpUrl = rawJump.startsWith("//") ? "https:" + rawJump : rawJump;

            JSONObject finalItem = enrichIfNeeded(firstItem);
            return new BiliData(publishTs, userFace, userId, jumpUrl, finalItem);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException("§7获取B站动态失败: " + e.getMessage(), e);
        }
    }

    private static JSONObject enrichIfNeeded(JSONObject item) {
        String type = item.optString("type", "");
        if ("DYNAMIC_TYPE_AV".equals(type))
            return item;
        String idStr = item.optString("id_str", "");
        if (idStr.isBlank())
            return item;
        JSONObject detail = HttpsHandler.fetchDetail(idStr);
        return detail != null ? detail : item;
    }

    private static long parseLongOrZero(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public Long getPublishTs() {
        return publishTs;
    }

    public String getUserFace() {
        return userFace;
    }

    public String getUserId() {
        return userId;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public JSONObject getLatestItem() {
        return latestItem;
    }
}
