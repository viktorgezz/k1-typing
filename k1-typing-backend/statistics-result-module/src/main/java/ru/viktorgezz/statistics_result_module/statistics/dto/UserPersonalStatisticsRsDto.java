package ru.viktorgezz.statistics_result_module.statistics.dto;

import java.math.BigDecimal;

public record UserPersonalStatisticsRsDto(
        Long rankPlace,
        Long countContest,
        BigDecimal averageSpeed,
        Long firstPlacesCount
) {
}
