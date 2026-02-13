package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Входящее сообщение от клиента: завершение печати.
 *
 * @param durationSeconds время печати в секундах
 * @param speed           скорость печати (символов в минуту)
 * @param accuracy        точность (0.00 - 100.00)
 */
public record FinishMessage(
        @Min(value = 0, message = "Duration must be at least 0 second")
        long durationSeconds,

        @Min(value = 0, message = "Speed cannot be negative")
        int speed,

        @NotNull(message = "Accuracy is required")
        @DecimalMin(value = "0.00", message = "Accuracy cannot be negative")
        @DecimalMax(value = "100.00", message = "Accuracy cannot exceed 100")
        BigDecimal accuracy
) {
}
