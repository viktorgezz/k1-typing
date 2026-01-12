package ru.viktorgezz.k1_typing_backend.domain.contest.dto.rq;

import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;

public record CreationContestRqDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long idExercise,

        @NotNull
        Status status
) {
}
