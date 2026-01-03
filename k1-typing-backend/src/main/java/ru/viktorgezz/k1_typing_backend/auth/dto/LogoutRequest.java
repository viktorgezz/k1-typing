package ru.viktorgezz.k1_typing_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {
    
}
