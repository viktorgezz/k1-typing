package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.*;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;

import java.math.BigDecimal;

/**
 * Исходящее сообщение: игрок финишировал.
 *
 * @param idUser   ID пользователя
 * @param username имя пользователя
 * @param place    занятое место
 * @param speed    скорость печати (символов в минуту)
 * @param durationSeconds время печати в секундах
 * @param accuracy точность (0.00 - 100.00)
 */
public record PlayerFinishedMessage(
        @NotNull(message = "User ID is required")
        Long idUser,

        @NotBlank(message = "User name is required")
        String username,

        @NotNull(message = "Place is required")
        Place place,

        @Min(value = 0, message = "Speed cannot be negative")
        int speed,

        @Min(value = 0, message = "Duration must be at least 0 second")
        long durationSeconds,

        @NotNull(message = "Accuracy is required")
        @DecimalMin(value = "0.00", message = "Accuracy cannot be negative")
        @DecimalMax(value = "100.00", message = "Accuracy cannot exceed 100")
        BigDecimal accuracy
) {
}
