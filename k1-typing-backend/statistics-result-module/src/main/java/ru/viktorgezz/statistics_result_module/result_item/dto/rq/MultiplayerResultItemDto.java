package ru.viktorgezz.statistics_result_module.result_item.dto.rq;


import ru.viktorgezz.statistics_result_module.result_item.Place;

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
