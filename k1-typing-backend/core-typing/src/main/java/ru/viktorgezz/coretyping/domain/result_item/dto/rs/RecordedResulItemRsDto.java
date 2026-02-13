package ru.viktorgezz.coretyping.domain.result_item.dto.rs;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.viktorgezz.coretyping.domain.result_item.Place;

/**
 * DTO для записанного результата выполнения упражнения.
 *
 * @param durationSeconds продолжительность в секундах
 * @param speed скорость (символов в минуту)
 * @param accuracy точность (0-100)
 * @param place место в рейтинге
 */
public record RecordedResulItemRsDto(
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

        @NotNull(message = "Место в рейтинге не может быть null")
        Place place
) {
}
