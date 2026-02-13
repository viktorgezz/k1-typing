package ru.viktorgezz.coretyping.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для обновления данных пользователя.
 *
 * @param username имя пользователя
 */
public record UserUpdatedRequest(
        @NotBlank(message = "Имя не должно быть пустым")
        @Size(
                min = 2,
                max = 255,
                message = "Минимальная длина имени должна быть 2, а максимальная 255"
        )
        String username,
        @NotBlank(message = "Пароль не должен быть пустым")
        String newPassword
) {
}
