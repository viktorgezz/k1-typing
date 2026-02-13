package ru.viktorgezz.coretyping.domain.exercises.dto.rs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.viktorgezz.coretyping.domain.exercises.Language;

public record ExerciseListItemRsDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long id,
        @NotBlank(message = "Название упражнения не должен быть пустым")
        @Size(
                min = 1,
                max = 200,
                message = "Длина названия упражнения должна быть от 1 до 200 символов"
        )
        String title,
        String nameTile,
        @NotNull(message = "Язык упражнения не должен быть пустым")
        Language language
) {
}
