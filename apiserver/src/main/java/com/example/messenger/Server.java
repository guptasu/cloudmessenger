package com.example.messenger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.*;

@ServerEndpoint("/hello")
public class Server {
    static Map<String, Session> sessionMapper = new HashMap<>();
    static Map<Session, String> sessionMapperReverse = new HashMap<>();

    public Server() {
        System.out.println("class loaded " + this.getClass());
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.printf("Session opened, id: %s%n", session.getId());

        try {
            session.getBasicRemote().sendText("Hi there, we are successfully connected.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        System.out.printf("Message received. Session id: %s Message: %s%n",
                session.getId(), message);
        try {
            if (message.startsWith("UID:")) {
                setUserToSession(message, session);
            } else if (message.contains(":")) {
                SendP2P(message, session);
            } else {
                session.getBasicRemote().sendText(String.format("We received your message: %s%n", message));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setUserToSession(String message, Session session) throws IOException {
        session.getBasicRemote().sendText(String.format(
                "Established connection with user %s%n", message.substring("UID:".length())));
        sessionMapper.put(message.substring("UID:".length()), session);
        sessionMapperReverse.put(session, message.substring("UID:".length()));
    }

    private void SendP2P(String message, Session session) throws IOException {
        String receiver = message.substring(0, message.indexOf(":"));
        String msgToTransfer = message.indexOf(":") + 1 >= message.length() ? "" : message.substring(message.indexOf(":") + 1);
        if (sessionMapper.containsKey(receiver)) {
            System.out.printf("Sending msg '%s' to '%s'.\n",
                    msgToTransfer, receiver);
            sessionMapper.get(receiver).getBasicRemote().sendText(String.format(
                    "Received:%s:From:%s\n", msgToTransfer, sessionMapperReverse.get(session)));
        } else {
            System.out.printf("Failed to send '%s' to '%s'. User not found\n",
                    msgToTransfer, receiver);
        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.printf("Session closed with id: %s%n", session.getId());
    }
}