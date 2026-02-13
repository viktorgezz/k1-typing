package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.NotBlank;

/**
 * Исходящее сообщение: ошибка.
 *
 * @param code код ошибки
 * @param message описание ошибки
 */
public record ErrorMessage(
        @NotBlank(message = "Error code is required")
        String code,

        @NotBlank(message = "Error message is required")
        String message
) {}
