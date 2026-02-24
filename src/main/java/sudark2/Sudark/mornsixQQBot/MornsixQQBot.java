package sudark2.Sudark.mornsixQQBot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import sudark2.Sudark.mornsixQQBot.onebot.OneBotClient;

import java.net.URI;
import java.net.URISyntaxException;

import static sudark2.Sudark.mornsixQQBot.FileManager.initFiles;
import static sudark2.Sudark.mornsixQQBot.schedule.Clock.start;

public final class MornsixQQBot extends JavaPlugin {

    public static URI ServerURI;
    public static WebSocketClient client;

    @Override
    public void onEnable() {
        try {
            ServerURI = new URI("ws://127.0.0.1:3001");
            client = new OneBotClient();
            client.connect();
        } catch (URISyntaxException e) {
            getLogger().warning("§7WebSocket 地址格式错误，插件未能连接 OneBot");
        }

        initFiles();
        start();
    }

    @Override
    public void onDisable() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception ignored) {
        }
    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("MornsixQQBot");
    }
}
