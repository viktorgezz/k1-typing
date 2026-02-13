package ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Исходящее сообщение: прогресс всех участников (broadcast).
 *
 * @param usersProgress карта idUser → данные прогресса участника
 */
public record AllProgressMessage(
        @NotNull(message = "Users progress map is required")
        Map<Long, UserProgressData> usersProgress
) {
    /**
     * Данные прогресса одного участника.
     *
     * @param progress процент прогресса (0-100)
     * @param speed    скорость (символов в минуту)
     * @param accuracy точность (0-100%)
     */
    public record UserProgressData(
            int progress,
            int speed,
            BigDecimal accuracy
    ) {
    }
}
