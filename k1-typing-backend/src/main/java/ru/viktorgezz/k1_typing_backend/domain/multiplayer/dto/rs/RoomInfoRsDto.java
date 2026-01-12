package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Полная информация о комнате соревнования.
 *
 * @param idContest        ID контеста
 * @param idExercise       ID упражнения
 * @param titleExercise    название упражнения
 * @param exerciseText     текст упражнения (передаётся если статус PROGRESS или FINISHED)
 * @param exerciseLanguage язык упражнения (RU или ENG)
 * @param status           текущий статус комнаты
 * @param currentPlayers   текущее количество игроков
 * @param maxPlayers       максимальное количество игроков
 * @param participants     список участников
 * @param createdAt        дата создания
 */
public record RoomInfoRsDto(
        @NotNull(message = "Contest ID is required")
        Long idContest,

        @NotNull(message = "Exercise ID is required")
        Long idExercise,

        @NotBlank(message = "Exercise title is required")
        String titleExercise,

        String exerciseText,

        @NotBlank(message = "Exercise language is required")
        String exerciseLanguage,

        @NotNull(message = "Status is required")
        Status status,

        @Min(value = 0, message = "Current players cannot be negative")
        int currentPlayers,

        @Min(value = 2, message = "Max players must be at least 2")
        int maxPlayers,

        @NotNull(message = "Participants list is required")
        @Valid
        List<ParticipantDto> participants,

        @NotNull(message = "Created at is required")
        LocalDateTime createdAt
) {
    /**
     * Информация об участнике комнаты.
     *
     * @param idUser   ID пользователя
     * @param username имя пользователя
     * @param isReady  готов ли игрок к старту
     * @param progress текущий прогресс (0-100)
     * @param speed    текущая скорость (символов в минуту)
     * @param accuracy текущая точность (0-100%)
     */
    public record ParticipantDto(
            @NotNull(message = "User ID is required")
            Long idUser,

            @NotBlank(message = "Username is required")
            String username,

            boolean isReady,

            int progress,

            int speed,

            java.math.BigDecimal accuracy
    ) {
    }

}
