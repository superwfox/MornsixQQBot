package sudark2.Sudark.mornsixQQBot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

import static sudark2.Sudark.mornsixQQBot.FileManager.initFiles;

public final class MornsixQQBot extends JavaPlugin {

    static URI ServerURI;

    static {
        try {
            ServerURI = new URI("127.0.0.1:8088");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static WebSocketClient client = null;

    @Override
    public void onEnable() {

        initFiles();
        Clock.start();
        client = new OneBotClient();

    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("MornsixQQBot");
    }
}
