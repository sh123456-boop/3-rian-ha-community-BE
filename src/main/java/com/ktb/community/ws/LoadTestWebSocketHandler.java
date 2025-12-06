//package com.ktb.community.ws;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Lightweight WebSocket handler for load/connect tests.
// * Counts concurrent connections and keeps sockets alive with minimal logic.
// */
//@Slf4j
//@Component
//public class LoadTestWebSocketHandler extends TextWebSocketHandler {
//
//    private final AtomicInteger activeConnections = new AtomicInteger();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        int current = activeConnections.incrementAndGet();
//        log.debug("LoadTest WS connected. sessionId={}, active={}", session.getId(), current);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // Optional echo for quick health checks
//        if ("ping".equalsIgnoreCase(message.getPayload())) {
//            session.sendMessage(new TextMessage("pong"));
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        int current = activeConnections.decrementAndGet();
//        log.debug("LoadTest WS disconnected. sessionId={}, active={}, status={}", session.getId(), current, status);
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) {
//        log.warn("LoadTest WS transport error. sessionId={}, message={}", session.getId(), exception.getMessage());
//    }
//}
