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
    static WebSocketClient client;

    static {
        try {
            ServerURI = new URI("ws://127.0.0.1:3001");
            client = new OneBotClient();
            client.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onEnable() {

        initFiles();
        Clock.start();

    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("MornsixQQBot");
    }
}
