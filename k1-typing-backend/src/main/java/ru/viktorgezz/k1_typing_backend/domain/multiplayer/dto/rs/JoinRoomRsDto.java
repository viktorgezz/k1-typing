package ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Ответ при создании или присоединении к комнате.
 *
 * @param idContest ID контеста
 * @param status    статус операции (SUCCESS, ROOM_FULL, NOT_FOUND и т.д.)
 * @param message   описание результата
 */
public record JoinRoomRsDto(

        @NotNull(message = "Contest ID is required")
        Long idContest,

        @NotBlank(message = "Status is required")
        JoinRoomStatus status,

        String message
) {
    /**
     * Статусы операции присоединения к комнате.
     */
    public enum JoinRoomStatus {
        SUCCESS,           // Успешно присоединился/создал комнату
        ROOM_FULL,         // Комната заполнена
        NOT_FOUND,         // Комната не найдена
        ALREADY_STARTED    // Соревнование уже началось, присоединиться нельзя
    }
}
