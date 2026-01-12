package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Исходящее сообщение: игрок присоединился к комнате.
 *
 * @param userId         ID пользователя
 * @param userName       имя пользователя
 * @param currentPlayers текущее количество игроков
 * @param maxPlayers     максимальное количество игроков
 */
public record PlayerJoinedMessage(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "User name is required")
        String userName,

        @Min(value = 1, message = "Current players must be at least 1")
        int currentPlayers,

        @Min(value = 2, message = "Max players must be at least 2")
        int maxPlayers
) {
}
