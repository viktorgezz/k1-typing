package ru.viktorgezz.avatarmodule.exception;

import lombok.Getter;

/**
 * Исключение с кодом ошибки {@link ErrorCode}.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String[] args;

    public BusinessException(ErrorCode errorCode, String... args) {
        super(getFormatterMsg(errorCode, args));
        this.errorCode = errorCode;
        this.args = args;
    }

    private static String getFormatterMsg(ErrorCode msg, Object[] args) {
        if (args == null || args.length == 0) {
            return msg.getDefaultMessage();
        }

        return String.format(msg.getDefaultMessage(), args);
    }
}
