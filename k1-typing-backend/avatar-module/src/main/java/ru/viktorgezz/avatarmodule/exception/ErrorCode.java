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

    AVATAR_NOT_FOUND(Constants.NOT_FOUND, "Avatar not found for user with id: %s", HttpStatus.NOT_FOUND),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    private static class Constants {
        private static final String NOT_FOUND = "NOT_FOUND";
    }
}
