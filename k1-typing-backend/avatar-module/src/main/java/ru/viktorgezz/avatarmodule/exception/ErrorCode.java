package ru.viktorgezz.avatarmodule.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Перечень кодов и шаблонов сообщений ошибок бизнес-логики.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    IMAGE_GENERATION_LIMIT_EXCEEDED("LIMIT_EXCEEDED", "Rate limit exceeded. Only 1 image per minute is allowed for user: %s", HttpStatus.TOO_MANY_REQUESTS),
    IMAGE_GENERATION_API_ERROR("EXTERNAL_API_ERROR", "Error during image generation: %s", HttpStatus.BAD_GATEWAY),
    IMAGE_GENERATION_MODEL_LOADING("MODEL_LOADING", "Model is currently loading. Please try again in 20 seconds.", HttpStatus.SERVICE_UNAVAILABLE),
    AVATAR_NOT_FOUND(Constants.NOT_FOUND, "Avatar not found for user with id: %s", HttpStatus.NOT_FOUND),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    private static class Constants {
        private static final String NOT_FOUND = "NOT_FOUND";
    }
}
