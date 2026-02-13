package ru.viktorgezz.coretyping.domain.exercises.dto.rq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatedExerciseRqDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long id,
        @NotBlank(message = "Название упражнения не должен быть пустым")
        @Size(
                min = 1,
                max = 200,
                message = "Длина названия упражнения должна быть от 1 до 200 символов"
        )
        String title,
        @NotBlank(message = "Текст упражнения не должен быть пустым")
        @Size(
                min = 10,
                max = 10000,
                message = "Длина текста упражнения должна быть от 10 до 10000 символов"
        )
        String text
) {
}
