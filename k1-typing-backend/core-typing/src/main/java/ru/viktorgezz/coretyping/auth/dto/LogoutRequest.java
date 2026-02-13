package ru.viktorgezz.coretyping.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {
    
}
