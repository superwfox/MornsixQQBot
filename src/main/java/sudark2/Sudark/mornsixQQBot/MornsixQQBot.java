package sudark2.Sudark.mornsixQQBot;

import org.java_websocket.client.WebSocketClient;
import sudark2.Sudark.mornsixQQBot.onebot.OneBotClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import static sudark2.Sudark.mornsixQQBot.FileManager.initFiles;
import static sudark2.Sudark.mornsixQQBot.schedule.Clock.start;

public final class MornsixQQBot {

    public static final Logger logger = Logger.getLogger("MornsixQQBot");

    public static URI ServerURI;
    public static WebSocketClient client;

    public static void main(String[] args) {
        String wsUrl = System.getenv().getOrDefault("MORNSIX_WS_URL", "ws://127.0.0.1:3001");
        try {
            ServerURI = new URI(wsUrl);
            client = new OneBotClient();
            client.connect();
        } catch (URISyntaxException e) {
            logger.warning("WebSocket 地址格式错误，未能连接 OneBot: " + wsUrl);
        }

        initFiles();
        start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (client != null) client.close();
            } catch (Exception ignored) {
            }
        }, "MornsixQQBot-Shutdown"));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
