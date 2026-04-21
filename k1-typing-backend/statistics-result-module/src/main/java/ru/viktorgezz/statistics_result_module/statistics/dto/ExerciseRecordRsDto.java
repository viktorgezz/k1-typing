package ru.viktorgezz.statistics_result_module.statistics.dto;

public record ExerciseRecordRsDto(
        String title,
        String username,
        Integer maxSpeed,
        Long minDuration
) {
}
