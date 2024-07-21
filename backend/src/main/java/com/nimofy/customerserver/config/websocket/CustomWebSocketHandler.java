package com.nimofy.customerserver.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private static final String NULL_USER_ID = "null";
    private final Map<Long, WebSocketSession> userIdWebSocketSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        extractUserIdParameter(session)
                .ifPresent(userId -> userIdWebSocketSessionMap.put(userId, session));
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) {
        log.info("Adding websocket session for userId [{}]", message.getPayload());
        userIdWebSocketSessionMap.put(Long.valueOf((String) message.getPayload()), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userIdWebSocketSessionMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(session))
                .findFirst()
                .ifPresent(entry -> {
                    try {
                        log.info("Remove Socket Session for userId [{}]", entry.getKey());
                        userIdWebSocketSessionMap.remove(entry.getKey()).close();
                    } catch (IOException e) {
                        log.error("Could not close Websocket session for userId:{}", entry.getKey(), e);
                    }
                });
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public Map<Long, WebSocketSession> getWebSocketSessions() {
        return userIdWebSocketSessionMap;
    }

    private Optional<Long> extractUserIdParameter(WebSocketSession session) {
        if (session.getUri() == null || session.getUri().getQuery() == null) {
            return Optional.empty();
        }

        String userIdString = extractUserId(session.getUri().getQuery());
        if (userIdString == null || NULL_USER_ID.equals(userIdString)) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(userIdString));
    }

    private String extractUserId(String query) {
        String[] queryParams = query.split("=");
        if (queryParams.length != 2) return null;
        return queryParams[1];
    }
}
