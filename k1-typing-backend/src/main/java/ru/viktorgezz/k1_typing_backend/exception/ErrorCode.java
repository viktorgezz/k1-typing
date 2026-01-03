package ru.viktorgezz.k1_typing_backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Перечень кодов и шаблонов сообщений ошибок бизнес-логики.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("NOT_FOUND", "User with username: %s - not found", HttpStatus.NOT_FOUND),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
    TOKEN_REFRESH_EXPIRED("UNAUTHORIZED", "JWT token is expired",  HttpStatus.UNAUTHORIZED),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Current password and new password are not the same", HttpStatus.BAD_REQUEST),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;
}
