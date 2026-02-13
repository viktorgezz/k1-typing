package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Исходящее сообщение: обратный отсчёт перед стартом.
 *
 * @param seconds оставшиеся секунды (5, 4, 3, 2, 1, 0)
 */
public record CountdownMessage(
        @Min(value = 0, message = "Seconds cannot be negative")
        @Max(value = 10, message = "Countdown cannot exceed 10 seconds")
        int seconds
) {
}
