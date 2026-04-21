package ru.viktorgezz.statistics_result_module.statistics.dto;

import java.math.BigDecimal;

public record UserLeaderboardRsDto(
        Long rankPlace,
        String username,
        BigDecimal averageSpeed,
        Long countContest,
        Long firstPlacesCount
) {
}
