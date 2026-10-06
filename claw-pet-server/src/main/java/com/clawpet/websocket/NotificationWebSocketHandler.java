package com.clawpet.websocket;

import com.clawpet.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    /** userId -> WebSocketSession（一个用户可能有多个连接） */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final JwtUtil jwtUtil;

    public NotificationWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从查询参数中获取 token
        URI uri = session.getUri();
        if (uri == null) {
            closeSession(session, CloseStatus.BAD_DATA);
            return;
        }
        String query = uri.getQuery();
        if (query == null || !query.startsWith("token=")) {
            closeSession(session, CloseStatus.POLICY_VIOLATION);
            return;
        }
        String token = query.substring(6); // 去掉 "token="

        if (!jwtUtil.validateToken(token)) {
            log.warn("WebSocket 连接失败：无效 token");
            closeSession(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        sessions.put(userId, session);
        log.info("WebSocket 连接成功：userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 移除断开的 session
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.info("WebSocket 断开：sessionId={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 暂不处理客户端发来的消息
    }

    /** 向指定用户推送通知消息 */
    public void sendNotification(Long userId, String messageJson) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(messageJson));
            } catch (Exception e) {
                log.error("WebSocket 推送失败：userId={}", userId, e);
                sessions.remove(userId);
            }
        }
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
        }
    }
}
