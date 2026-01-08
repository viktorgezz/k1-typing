package ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Language;

/**
 * DTO for {@link ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise}
 */
public record ExerciseGetByIdRsDto(
        @NotNull(message = "ID упражнения не должен быть пустым")
        Long id,
        @NotBlank(message = "Текст упражнения не должен быть пустым")
        @Size(
                min = 10,
                max = 10000,
                message = "Длина текста упражнения должна быть от 10 до 10000 символов"
        )
        String text,
        Language language
) {
    public ExerciseGetByIdRsDto(Exercise exercise) {
        this(exercise.getId(), exercise.getText(), exercise.getLanguage());
    }
}