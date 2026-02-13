package ru.viktorgezz.avatarmodule.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Утилитный класс для получения id текущего аутентифицированного пользователя
 * из JWT-токена, переданного в заголовке {@code Authorization: Bearer <token>}.
 *
 * <p>
 * Парсит токен напрямую с использованием публичного RSA-ключа,
 * без зависимости на модуль {@code core-typing}.
 * </p>
 */
public class CurrentUserUtils {

    private static final String ID_USER_CLAIM = "id_user";
    private static final PublicKey PUBLIC_KEY;

    static {
        try {
            PUBLIC_KEY = KeyUtils.loadPublicKey("keys/public_key.pem");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private CurrentUserUtils() {
    }

    /**
     * Получает id текущего аутентифицированного пользователя из JWT-токена.
     *
     * @return id текущего пользователя
     * @throws AuthenticationServiceException если токен отсутствует, невалиден
     *                                        или не содержит claim {@code id_user}
     */
    public static Long getCurrentIdUser() {
        final String token = extractToken();
        final Claims claims = parseToken(token);

        final Object idUser = claims.get(ID_USER_CLAIM);
        if (idUser == null) {
            throw new AuthenticationServiceException(
                    "JWT token does not contain '" + ID_USER_CLAIM + "' claim");
        }

        return ((Number) idUser).longValue();
    }

    private static String extractToken() {
        final ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        if (attributes == null) {
            throw new AuthenticationServiceException("No HTTP request context available");
        }

        final HttpServletRequest request = attributes.getRequest();
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationServiceException("Missing or invalid Authorization header");
        }

        return authHeader.substring(7);
    }

    private static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(PUBLIC_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid JWT token: " + e.getMessage(), e);
        }
    }
}
