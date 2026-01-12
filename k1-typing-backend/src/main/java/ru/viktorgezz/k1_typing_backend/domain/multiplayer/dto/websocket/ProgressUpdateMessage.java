package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Входящее сообщение от клиента: обновление прогресса печати.
 *
 * @param progress процент прогресса (0-100)
 * @param speed    текущая скорость (символов в минуту)
 * @param accuracy текущая точность (0-100%)
 */
public record ProgressUpdateMessage(
        @Min(value = 0, message = "Progress cannot be negative")
        @Max(value = 100, message = "Progress cannot exceed 100")
        int progress,

        @Min(value = 0, message = "Speed cannot be negative")
        int speed,

        @DecimalMin(value = "0.00", message = "Accuracy cannot be negative")
        @DecimalMax(value = "100.00", message = "Accuracy cannot exceed 100")
        BigDecimal accuracy
) {
}
