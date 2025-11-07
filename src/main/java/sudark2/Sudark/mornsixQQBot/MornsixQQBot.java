package sudark2.Sudark.mornsixQQBot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

import static sudark2.Sudark.mornsixQQBot.FileManager.initFiles;

public final class MornsixQQBot extends JavaPlugin {

    static URI serverUri;

    static {
        try {
            serverUri = new URI("127.0.0.1:8088");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    WebSocketClient client = new OneBotClient();

    @Override
    public void onEnable() {
        initFiles();
    }

    public static Plugin get(){
        return Bukkit.getPluginManager().getPlugin("MornsixQQBot");
    }
}
