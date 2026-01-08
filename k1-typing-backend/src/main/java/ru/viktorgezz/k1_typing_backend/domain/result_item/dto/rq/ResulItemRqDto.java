package ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResulItemRqDto(
        @NotNull(message = "Продолжительность не может быть null")
        @Positive(message = "Продолжительность должна быть положительным числом")
        Long durationSeconds,

        @NotNull(message = "Скорость не может быть null")
        @Positive(message = "Скорость должна быть положительным числом")
        Integer speed,

        @NotNull(message = "Точность не может быть null")
        @DecimalMin(value = "0.0", message = "Точность не может быть меньше 0")
        @DecimalMax(value = "100.0", message = "Точность не может быть больше 100")
        BigDecimal accuracy,

        @NotNull(message = "ID контеста не может быть null")
        @Positive(message = "ID контеста должен быть положительным числом")
        Long idContest
) {
}
