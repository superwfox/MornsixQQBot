package sudark2.Sudark.mornsixQQBot.EmailRelated;

import jakarta.mail.AuthenticationFailedException;

import java.util.List;

import static sudark2.Sudark.mornsixQQBot.FileManager.ManagerGroup;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.logger;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotApi.sendG;

public class EmailChecker {

    public static void checkEmails() {
        if (!EmailConfig.isConfigured())
            return;

        try {
            List<EmailMessage> emails = ImapSmtpClient.getUnreadEmails();

            for (EmailMessage email : emails) {
                try {
                    EmailFormatter.sendEmailToManager(email);
                } catch (Exception e) {
                    warn("§7处理邮件失败: " + e.getMessage());
                }
            }

        } catch (AuthenticationFailedException e) {
            sendG("⚠️ 邮箱登录失败，请检查授权码\n使用命令: /setEmail 邮箱地址 授权码", ManagerGroup);
            warn("§7邮箱登录失败");
        } catch (Exception e) {
            warn("§7邮件扫描失败: " + e.getMessage());
        }
    }

    private static void warn(String msg) {
        logger.warning(msg);
    }
}
