package ru.viktorgezz.coretyping.security.websocket;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.viktorgezz.coretyping.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.coretyping.security.exception.TokenExpiredException;
import ru.viktorgezz.coretyping.security.service.JwtService;

import java.util.List;

/**
 * Интерцептор для аутентификации WebSocket соединений по JWT.
 * При CONNECT команде извлекает JWT токен из заголовка Authorization
 * и устанавливает аутентификацию пользователя.
 * Клиент должен передать токен в заголовке:
 * - STOMP header: Authorization: Bearer {token}
 * - или как query параметр: /ws/contest?token={token}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<@NotNull ?> preSend(
            @Nullable Message<?> message,
            @Nullable MessageChannel channel
    ) {
        if (message == null) {
            return null;
        }

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        // Аутентификация только при CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateUser(accessor);
        }

        return message;
    }

    private void authenticateUser(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);

        if (token == null) {
            log.debug("WebSocket connection without token - allowing anonymous");
            return;
        }

        try {
            String username = jwtService.extractUsername(token);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Устанавливаем аутентификацию в WebSocket сессию
                    accessor.setUser(authentication);

                    // Также устанавливаем в SecurityContext для текущего потока
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("WebSocket authenticated user: {}", username);
                } else {
                    log.warn("Invalid JWT token for WebSocket connection");
                }
            }
        } catch (TokenExpiredException e) {
            log.debug("WebSocket connection with expired token: {}", e.getMessage());
        } catch (InvalidJwtTokenException e) {
            log.debug("WebSocket connection with invalid token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error during WebSocket authentication: {}", e.getMessage());
        }
    }

    /**
     * Извлекает JWT токен из STOMP заголовков.
     * Поддерживает форматы:
     * - Authorization: Bearer {token}
     * - Authorization: {token}
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // Пробуем получить из нативных STOMP заголовков
        List<String> authHeaders = accessor.getNativeHeader(AUTHORIZATION_HEADER);

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.getFirst();

            if (authHeader.startsWith(BEARER_PREFIX)) {
                return authHeader.substring(BEARER_PREFIX.length());
            }

            // Если токен передан без Bearer prefix
            return authHeader;
        }

        return null;
    }
}
