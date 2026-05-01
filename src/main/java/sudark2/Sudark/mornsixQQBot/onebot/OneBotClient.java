package sudark2.Sudark.mornsixQQBot.onebot;

import net.sf.json.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.ServerURI;
import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.logger;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotEchoStore.handleEcho;
import static sudark2.Sudark.mornsixQQBot.onebot.OneBotEventRouter.handle;

public class OneBotClient extends WebSocketClient {

    public OneBotClient() {
        super(ServerURI);
    }

    @Override
    public void onMessage(String s) {
        JSONObject json = JSONObject.fromObject(s);
        handleEcho(json);
        handle(json);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
    }

    @Override
    public void onError(Exception e) {
        logger.warning("OneBot 连接异常");
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }
}
