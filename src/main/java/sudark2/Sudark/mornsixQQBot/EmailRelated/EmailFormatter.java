package sudark2.Sudark.mornsixQQBot.EmailRelated;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bukkit.plugin.Plugin;

import java.util.Base64;
import java.util.List;

import static sudark2.Sudark.mornsixQQBot.FileManager.ManagerGroup;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendAction;

public class EmailFormatter {

    public static void sendEmailToManager(EmailMessage email) {
        try {
            JSONObject root = new JSONObject();
            root.put("group_id", ManagerGroup);

            JSONArray messageArr = new JSONArray();

            // 构建文本段
            StringBuilder text = new StringBuilder();
            text.append("📧 新邮件\n");
            text.append("发件人: ").append(email.getFrom()).append("\n");
            text.append("主题: ").append(email.getSubject()).append("\n");
            text.append("时间: ").append(formatDateTime(email.getReceivedDateTime())).append("\n\n");
            text.append(email.getBodyPreview());

            JSONObject textMsg = new JSONObject();
            textMsg.put("type", "text");
            JSONObject textData = new JSONObject();
            textData.put("text", text.toString());
            textMsg.put("data", textData);
            messageArr.add(textMsg);

            // 处理图片附件（最多5张）
            List<EmailMessage.EmailAttachment> attachments = email.getAttachments();
            int imageCount = 0;
            for (EmailMessage.EmailAttachment attachment : attachments) {
                if (imageCount >= 5)
                    break;
                if (!attachment.isImage())
                    continue;

                byte[] imageData = attachment.getData();
                if (imageData != null && imageData.length > 0) {
                    String base64 = Base64.getEncoder().encodeToString(imageData);
                    JSONObject imageMsg = new JSONObject();
                    imageMsg.put("type", "image");
                    JSONObject imageData2 = new JSONObject();
                    imageData2.put("file", "base64://" + base64);
                    imageMsg.put("data", imageData2);
                    messageArr.add(imageMsg);
                    imageCount++;
                }
            }

            root.put("message", messageArr);
            JSONObject finalJson = new JSONObject();
            finalJson.put("action", "send_group_msg");
            finalJson.put("params", root);
            sendAction(finalJson, "§7发送邮件通知失败");

        } catch (Exception e) {
            warn("§7格式化邮件失败: " + e.getMessage());
        }
    }

    private static String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty())
            return "未知时间";
        try {
            // ISO 8601格式: 2026-03-17T14:30:00Z
            String[] parts = isoDateTime.split("T");
            if (parts.length < 2)
                return isoDateTime;
            String date = parts[0];
            String time = parts[1].substring(0, Math.min(8, parts[1].length()));
            return date + " " + time;
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null)
            plugin.getLogger().warning(msg);
    }
}
