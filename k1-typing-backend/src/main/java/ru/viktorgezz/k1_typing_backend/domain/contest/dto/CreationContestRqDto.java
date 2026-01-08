package ru.viktorgezz.k1_typing_backend.domain.contest.dto;

import jakarta.validation.constraints.NotNull;

public record CreationContestRqDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long idExercise
) {
}
