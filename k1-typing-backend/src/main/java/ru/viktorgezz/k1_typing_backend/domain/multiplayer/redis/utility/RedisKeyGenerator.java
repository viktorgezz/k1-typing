package ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility;

import static java.lang.String.format;

import java.time.Duration;

/**
 * Константы и генерация ключей для Redis.
 */
public class RedisKeyGenerator {
    private RedisKeyGenerator() {
    }

    public static final Duration TTL_ROOM = Duration.ofMinutes(30);

    private static final String KEY_INFO = "contest:%d:info";
    private static final String KEY_PARTICIPANTS = "contest:%d:participants";
    private static final String KEY_USERNAMES = "contest:%d:usernames";
    private static final String KEY_PROGRESS = "contest:%d:progress";
    private static final String KEY_FINISHERS = "contest:%d:finishers";
    private static final String KEY_READY = "contest:%d:ready";

    public static final String FIELD_EXERCISE_ID = "idExercise";
    public static final String FIELD_MAX_PARTICIPANTS = "participantsMax";

    public static String keyInfo(Long idContest) {
        return format(KEY_INFO, idContest);
    }

    public static String keyParticipants(Long idContest) {
        return format(KEY_PARTICIPANTS, idContest);
    }

    public static String keyUsernames(Long idContest) {
        return format(KEY_USERNAMES, idContest);
    }

    public static String keyProgress(Long idContest) {
        return format(KEY_PROGRESS, idContest);
    }

    public static String keyFinishers(Long idContest) {
        return format(KEY_FINISHERS, idContest);
    }

    public static String keyReady(Long idContest) {
        return format(KEY_READY, idContest);
    }
}
