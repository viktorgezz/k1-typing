package ru.viktorgezz.coretyping.domain.contest.dto.rq;

import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.coretyping.domain.contest.Status;

public record CreationContestRqDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long idExercise,

        @NotNull
        Status status
) {
}
