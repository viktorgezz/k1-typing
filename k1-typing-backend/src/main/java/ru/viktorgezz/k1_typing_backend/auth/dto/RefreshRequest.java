package ru.viktorgezz.k1_typing_backend.auth.dto;

/**
 * Запрос на обновление access-токена по refresh-токену.
 *
 * @param refreshToken действующий refresh-токен
 */
public record RefreshRequest(
        String refreshToken
) {
}
