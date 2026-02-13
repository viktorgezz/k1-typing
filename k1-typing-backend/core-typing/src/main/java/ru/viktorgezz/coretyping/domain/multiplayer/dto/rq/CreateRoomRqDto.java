package ru.viktorgezz.coretyping.domain.multiplayer.dto.rq;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на создание комнаты соревнования.
 *
 * @param idExercise      ID упражнения для соревнования
 * @param maxParticipants максимальное количество участников (2-15)
 */
public record CreateRoomRqDto(
        @NotNull(message = "Exercise ID is required")
        Long idExercise,

        @Min(value = 2, message = "Minimum 2 participants required")
        @Max(value = 15, message = "Maximum 10 participants allowed")
        Integer maxParticipants
) {
}
