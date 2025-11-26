package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * 1. REST API Controller
     * Serves HTTPS REST endpoints.
     */
    @RestController
    public static class ApiController {
        
        @GetMapping("/api/hello")
        public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
            return String.format("Hello, %s! The server time is %s", name, LocalDateTime.now());
        }
    }

    /**
     * 2. WebSocket Configuration
     * Maps the WebSocket handler to a specific URL endpoint.
     */
    @Configuration
    @EnableWebSocket
    public static class WebSocketConfig implements WebSocketConfigurer {

        private final ChatHandler chatHandler = new ChatHandler();

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            // "allowedOrigins" is set to "*" for easier local testing. 
            // In production, restrict this to your specific domain.
            registry.addHandler(chatHandler, "/ws").setAllowedOriginPatterns("*");
        }
    }

    /**
     * 3. WebSocket Handler
     * Manages connections and message broadcasting.
     */
    public static class ChatHandler extends TextWebSocketHandler {

        // Thread-safe set to store active sessions
        private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.add(session);
            broadcast("New user joined! Total users: " + sessions.size());
        }

        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
            String receivedPayload = message.getPayload();
            // Echo the message to all connected clients
            broadcast("User " + session.getId() + " says: " + receivedPayload);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(session);
            broadcast("User left. Total users: " + sessions.size());
        }

        private void broadcast(String message) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}