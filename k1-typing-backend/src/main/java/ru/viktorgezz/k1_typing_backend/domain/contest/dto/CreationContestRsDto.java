package ru.viktorgezz.k1_typing_backend.domain.contest.dto;

import jakarta.validation.constraints.NotNull;

public record CreationContestRsDto(
        @NotNull(message = "ID контеста не должен быть пустым")
        Long idContest
) {
}
