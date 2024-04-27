package com.mygdx.game;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameWebSocketClient extends WebSocketClient {
	private MessageListener listener;

    public GameWebSocketClient(String uri, MessageListener listener) throws URISyntaxException {
        super(new URI(uri));
        this.listener = listener;
        setConnectionLostTimeout(60);
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connection opened");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        if (listener != null) {
            System.out.println("Forwarding to listener: " + listener.getClass().getSimpleName());
            listener.messageReceived(message);
        } else {
            System.out.println("No listener attached");
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

    @Override
    public void sendPing() {
        sendFrame(new PingFrame());
        System.out.println("Ping sent");
    }
    
}