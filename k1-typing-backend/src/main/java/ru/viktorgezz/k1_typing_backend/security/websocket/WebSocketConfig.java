package ru.viktorgezz.k1_typing_backend.security.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Конфигурация WebSocket с поддержкой STOMP протокола.
 * Endpoints:
 * - /ws/contest - основной WebSocket endpoint для соревнований
 * Message destinations:
 * - /topic/* - для broadcast сообщений (server → clients)
 * - /app/* - для сообщений от клиентов (client → server)
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Включаем простой брокер для подписок на /topic
        // Клиенты подписываются на /topic/contest/{idContest}/progress и т.д.
        config.enableSimpleBroker("/topic", "/queue");
        
        // Префикс для сообщений от клиентов к серверу
        // Клиенты отправляют на /app/contest/{idContest}/progress
        config.setApplicationDestinationPrefixes("/app");
        
        // Префикс для личных сообщений пользователю
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Основной WebSocket endpoint
        // Клиенты подключаются к ws://host/ws/contest
        registry.addEndpoint("/ws/contest")
                .setAllowedOriginPatterns(
                    "http://localhost:*",
                    "http://127.0.0.1:*"
                )
                .withSockJS(); // Fallback для браузеров без WebSocket поддержки
        
        // Также добавляем endpoint без SockJS для нативных WebSocket клиентов
        registry.addEndpoint("/ws/contest")
                .setAllowedOriginPatterns(
                    "http://localhost:*",
                    "http://127.0.0.1:*"
                );
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Добавляем интерцептор для JWT аутентификации
        registration.interceptors(authChannelInterceptor);
    }
}
