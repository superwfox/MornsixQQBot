package sudark2.Sudark.mornsixQQBot.EmailRelated;

import org.bukkit.plugin.Plugin;

import java.io.*;

import static sudark2.Sudark.mornsixQQBot.FileManager.FileFolder;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.get;

public class EmailConfig {
    private static String email = "";
    private static String accessToken = "";
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
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                email = parts[0];
                accessToken = parts[1];
            }
        } catch (IOException e) {
            warn("§7读取邮箱配置失败");
        }
    }

    public static void saveConfig(String newEmail, String newToken) {
        email = newEmail;
        accessToken = newToken;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(configFile))) {
            w.write(email + "|" + accessToken);
        } catch (IOException e) {
            warn("§7保存邮箱配置失败");
        }
    }

    public static String getEmail() {
        return email;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static boolean isConfigured() {
        return !email.isEmpty() && !accessToken.isEmpty();
    }

    public static String maskToken(String token) {
        if (token.length() <= 8)
            return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }

    private static void warn(String msg) {
        Plugin plugin = get();
        if (plugin != null)
            plugin.getLogger().warning(msg);
    }
}
