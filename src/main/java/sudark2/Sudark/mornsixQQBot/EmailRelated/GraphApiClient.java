package sudark2.Sudark.mornsixQQBot.EmailRelated;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class GraphApiClient {
    private static final String BASE_URL = "https://graph.microsoft.com/v1.0";

    public static List<EmailMessage> getUnreadEmails() throws Exception {
        String url = BASE_URL + "/me/messages?$filter=isRead eq false&$top=50&$orderby=receivedDateTime desc";
        JSONObject response = makeRequest(url, "GET", null);

        List<EmailMessage> emails = new ArrayList<>();
        if (response.optInt("code") == 401) {
            throw new TokenExpiredException("Access token expired");
        }

        JSONArray values = response.optJSONArray("value");
        if (values == null)
            return emails;

        for (int i = 0; i < values.size(); i++) {
            JSONObject item = values.getJSONObject(i);
            EmailMessage email = new EmailMessage();
            email.setId(item.optString("id"));
            email.setSubject(item.optString("subject", "(无主题)"));
            email.setBodyPreview(item.optString("bodyPreview", ""));
            email.setReceivedDateTime(item.optString("receivedDateTime", ""));

            JSONObject fromObj = item.optJSONObject("from");
            if (fromObj != null) {
                JSONObject emailAddr = fromObj.optJSONObject("emailAddress");
                if (emailAddr != null) {
                    email.setFrom(emailAddr.optString("address", "未知发件人"));
                }
            }

            if (item.optBoolean("hasAttachments", false)) {
                email.setAttachments(getAttachments(email.getId()));
            }

            emails.add(email);
        }

        return emails;
    }

    public static boolean markAsRead(String messageId) {
        try {
            String url = BASE_URL + "/me/messages/" + messageId;
            JSONObject body = new JSONObject();
            body.put("isRead", true);
            makeRequest(url, "PATCH", body.toString());
            return true;
        } catch (Exception e) {
            warn("§7标记邮件已读失败: " + e.getMessage());
            return false;
        }
    }

    public static List<EmailMessage.EmailAttachment> getAttachments(String messageId) {
        List<EmailMessage.EmailAttachment> attachments = new ArrayList<>();
        try {
            String url = BASE_URL + "/me/messages/" + messageId + "/attachments";
            JSONObject response = makeRequest(url, "GET", null);

            JSONArray values = response.optJSONArray("value");
            if (values == null)
                return attachments;

            for (int i = 0; i < values.size(); i++) {
                JSONObject item = values.getJSONObject(i);
                EmailMessage.EmailAttachment attachment = new EmailMessage.EmailAttachment();
                attachment.setId(item.optString("id"));
                attachment.setName(item.optString("name", "未命名附件"));
                attachment.setContentType(item.optString("contentType", ""));
                attachment.setSize(item.optInt("size", 0));
                attachment.setInline(item.optBoolean("isInline", false));
                attachments.add(attachment);
            }
        } catch (Exception e) {
            warn("§7获取附件列表失败: " + e.getMessage());
        }
        return attachments;
    }

    public static byte[] downloadAttachment(String messageId, String attachmentId) {
        try {
            String url = BASE_URL + "/me/messages/" + messageId + "/attachments/" + attachmentId + "/$value";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Authorization", "Bearer " + EmailConfig.getAccessToken());

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1)
                    baos.write(buf, 0, n);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            warn("§7下载附件失败: " + e.getMessage());
        }
        return null;
    }

    public static boolean testConnection() {
        try {
            String url = BASE_URL + "/me";
            JSONObject response = makeRequest(url, "GET", null);
            return response.has("mail") || response.has("userPrincipalName");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean sendEmail(String to, String subject, String body) {
        try {
            String url = BASE_URL + "/me/sendMail";
            JSONObject message = new JSONObject();
            JSONObject toRecipient = new JSONObject();
            JSONObject emailAddress = new JSONObject();
            emailAddress.put("address", to);
            toRecipient.put("emailAddress", emailAddress);

            JSONArray toRecipients = new JSONArray();
            toRecipients.add(toRecipient);

            message.put("subject", subject);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("contentType", "Text");
            bodyObj.put("content", body);
            message.put("body", bodyObj);
            message.put("toRecipients", toRecipients);

            JSONObject payload = new JSONObject();
            payload.put("message", message);
            payload.put("saveToSentItems", true);

            makeRequest(url, "POST", payload.toString());
            return true;
        } catch (Exception e) {
            warn("§7发送邮件失败: " + e.getMessage());
            return false;
        }
    }

    private static JSONObject makeRequest(String urlStr, String method, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setRequestProperty("Authorization", "Bearer " + EmailConfig.getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");

        if (body != null && (method.equals("POST") || method.equals("PATCH"))) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = readAll(is);

        if (code == 401) {
            JSONObject error = new JSONObject();
            error.put("code", 401);
            error.put("message", "Unauthorized");
            return error;
        }

        try {
            return JSONObject.fromObject(responseBody);
        } catch (Exception e) {
            JSONObject fallback = new JSONObject();
            fallback.put("code", code);
            fallback.put("message", responseBody);
            return fallback;
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null)
            return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1)
            baos.write(buf, 0, n);
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null)
            plugin.getLogger().warning(msg);
    }

    public static class TokenExpiredException extends Exception {
        public TokenExpiredException(String message) {
            super(message);
        }
    }
}
