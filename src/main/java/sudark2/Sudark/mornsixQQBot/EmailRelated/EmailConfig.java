package sudark2.Sudark.mornsixQQBot.EmailRelated;

import org.bukkit.plugin.Plugin;

import java.io.*;

import static sudark2.Sudark.mornsixQQBot.FileManager.FileFolder;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class EmailConfig {
    private static String email = "";
    private static String password = "";
    private static String imapHost = "outlook.office365.com";
    private static int imapPort = 993;
    private static String smtpHost = "smtp-mail.outlook.com";
    private static int smtpPort = 587;
    private static final File configFile = new File(FileFolder, "email_config.txt");

    public static void loadConfig() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                warn("§7创建邮箱配置文件失败");
            }
            return;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(configFile))) {
            String line = r.readLine();
            if (line == null || line.isEmpty())
                return;
            String[] parts = line.split("\\|");
            if (parts.length >= 2) {
                email = parts[0];
                password = parts[1];
            }
            if (parts.length >= 6) {
                imapHost = parts[2];
                imapPort = Integer.parseInt(parts[3]);
                smtpHost = parts[4];
                smtpPort = Integer.parseInt(parts[5]);
            }
            // 2个字段时自动使用Outlook默认值
        } catch (IOException e) {
            warn("§7读取邮箱配置失败");
        }
    }

    public static void saveConfig(String newEmail, String newPassword) {
        saveConfig(newEmail, newPassword, "outlook.office365.com", 993, "smtp-mail.outlook.com", 587);
    }

    public static void saveConfig(String newEmail, String newPassword,
                                  String newImapHost, int newImapPort,
                                  String newSmtpHost, int newSmtpPort) {
        email = newEmail;
        password = newPassword;
        imapHost = newImapHost;
        imapPort = newImapPort;
        smtpHost = newSmtpHost;
        smtpPort = newSmtpPort;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(configFile))) {
            w.write(email + "|" + password + "|" + imapHost + "|" + imapPort + "|" + smtpHost + "|" + smtpPort);
        } catch (IOException e) {
            warn("§7保存邮箱配置失败");
        }
    }

    public static String getEmail() { return email; }
    public static String getPassword() { return password; }
    public static String getImapHost() { return imapHost; }
    public static int getImapPort() { return imapPort; }
    public static String getSmtpHost() { return smtpHost; }
    public static int getSmtpPort() { return smtpPort; }

    public static boolean isConfigured() {
        return !email.isEmpty() && !password.isEmpty();
    }

    public static String maskPassword(String pwd) {
        if (pwd.length() <= 8)
            return "****";
        return pwd.substring(0, 4) + "****" + pwd.substring(pwd.length() - 4);
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null)
            plugin.getLogger().warning(msg);
    }
}
