package ru.viktorgezz.coretyping.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Перечень кодов и шаблонов сообщений ошибок бизнес-логики.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXERCISE_NOT_FOUND(Constants.NOT_FOUND, "Exercise - not found", HttpStatus.NOT_FOUND),
    CONTEST_NOT_FOUND(Constants.NOT_FOUND, "Contest with id: %s - not found ", HttpStatus.NOT_FOUND),
    CONTEST_ALREADY_STARTED("CONTEST_ALREADY_STARTED", "Contest with id: %s - already started", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND(Constants.NOT_FOUND, "Room with id contest: %s - not found", HttpStatus.NOT_FOUND),
    ROOM_ALREADY_FULL("ROOM_ALREADY_FULL", "Room with id contest: %s - is full", HttpStatus.BAD_REQUEST),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
    TOKEN_REFRESH_EXPIRED("UNAUTHORIZED", "JWT token is expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Current password and new password are not the same", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username: %s - already exists", HttpStatus.BAD_REQUEST),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    private static class Constants {
        private static final String NOT_FOUND = "NOT_FOUND";
    }
}
