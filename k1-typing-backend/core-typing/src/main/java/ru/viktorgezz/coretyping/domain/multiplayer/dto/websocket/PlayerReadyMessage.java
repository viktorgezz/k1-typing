package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.NotNull;

/**
 * Исходящее сообщение: игрок отметился готовым.
 *
 * @param idUser     ID пользователя
 * @param readyCount текущее количество готовых игроков
 * @param totalCount общее количество игроков в комнате
 */
public record PlayerReadyMessage(
        @NotNull(message = "User ID is required")
        Long idUser,

        int readyCount,

        int totalCount
) {
}
