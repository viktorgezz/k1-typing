package ru.viktorgezz.coretyping.domain.multiplayer.dto.rs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.coretyping.domain.exercises.Language;

import java.time.LocalDateTime;

/**
 * Краткая информация о комнате для списка доступных комнат.
 *
 * @param idContest      ID контеста
 * @param titleExercise  название упражнения
 * @param language       язык упражнения (RU, ENG)
 * @param currentPlayers текущее количество игроков
 * @param maxPlayers     максимальное количество игроков
 * @param createdAt      дата создания
 */
public record AvailableRoomRsDto(
        @NotNull(message = "Contest ID is required")
        Long idContest,

        @NotBlank(message = "Exercise title is required")
        String titleExercise,

        @NotNull(message = "Exercise language is required")
        Language language,

        @Min(value = 0, message = "Current players cannot be negative")
        int currentPlayers,

        @Min(value = 2, message = "Max players must be at least 2")
        int maxPlayers,

        @NotNull(message = "Created at is required")
        LocalDateTime createdAt
) {
}
