package com.mygdx.game;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameWebSocketClient extends WebSocketClient {
	private MessageListener listener;

    public GameWebSocketClient(String uri, MessageListener listener) throws URISyntaxException {
        super(new URI(uri));
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connection opened");
    }

    @Override
    public void onMessage(String message) {
        if (listener != null) {
            listener.messageReceived(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
    
}