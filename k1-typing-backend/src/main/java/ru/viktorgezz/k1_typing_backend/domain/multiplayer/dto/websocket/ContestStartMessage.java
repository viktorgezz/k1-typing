package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Исходящее сообщение: старт соревнования.
 *
 * @param text           текст для печати
 * @param startTimestamp Unix timestamp момента старта
 */
public record ContestStartMessage(
        @NotBlank(message = "Text is required")
        String text,

        @Positive(message = "Start timestamp must be positive")
        long startTimestamp
) {
}
