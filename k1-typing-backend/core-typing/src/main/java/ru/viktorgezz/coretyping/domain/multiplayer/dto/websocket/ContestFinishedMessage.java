package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ru.viktorgezz.coretyping.domain.result_item.Place;

import java.math.BigDecimal;
import java.util.List;

/**
 * Исходящее сообщение: соревнование завершено — финальные результаты.
 *
 * @param leaderboard таблица лидеров, отсортированная по местам
 */
public record ContestFinishedMessage(
        @NotNull(message = "Leaderboard is required")
        @Valid
        List<LeaderboardEntry> leaderboard
) {
    /**
     * Запись в таблице лидеров.
     *
     * @param idUser          ID пользователя
     * @param username        имя пользователя
     * @param place           занятое место
     * @param durationSeconds время печати в секундах
     * @param speed           скорость печати (символов в минуту)
     * @param accuracy        точность (0.00 - 100.00)
     */
    public record LeaderboardEntry(
            @NotNull(message = "User ID is required")
            Long idUser,

            @NotBlank(message = "User name is required")
            String username,

            @NotNull(message = "Place is required")
            Place place,

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
}
