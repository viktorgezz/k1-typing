package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Исходящее сообщение: игрок покинул комнату.
 *
 * @param idUser         ID пользователя
 * @param username       имя пользователя
 * @param currentPlayers текущее количество игроков после выхода
 */
public record PlayerLeftRoomMessage(
        @NotNull(message = "User ID is required")
        Long idUser,

        @NotBlank(message = "Username is required")
        String username,

        @Min(value = 0, message = "Current players cannot be negative")
        int currentPlayers
) {
}
