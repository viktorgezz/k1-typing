package ru.viktorgezz.coretyping.domain.result_item.dto.rq;

import ru.viktorgezz.coretyping.domain.result_item.Place;

import java.math.BigDecimal;

public record MultiplayerResultItemDto(
        Long idContest,
        Long idUser,
        long durationSeconds,
        int speed,
        BigDecimal accuracy,
        Place place
) {
}
