package sudark2.Sudark.mornsixQQBot;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.serverUri;

public class OneBotClient extends WebSocketClient {
    public OneBotClient() {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {



    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {
    }

    @Override
    public void onError(Exception e) {
    }
}
