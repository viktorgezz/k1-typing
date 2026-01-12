package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.keyReady;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ReadyService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import testconfig.AbstractIntegrationRedisTest;

class ReadyServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private ReadyService readyService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long ID_CONTEST_TEST = 5L;
    private static final Long ID_EXERCISE_TEST = 100L;
    private static final int PARTICIPANTS_MAX_TEST = 4;

    private static final Long ID_USER_FIRST = 10L;
    private static final Long ID_USER_SECOND = 20L;
    private static final Long ID_USER_THIRD = 30L;

    @BeforeEach
    void setupRoom() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);
    }

    @AfterEach
    void cleanupRedis() {
        redisTemplate.delete(keyReady(ID_CONTEST_TEST));
        roomService.deleteRoom(ID_CONTEST_TEST);
    }

    @Test
    @DisplayName("Отметка готовности сохраняет пользователя в множество готовых")
    void markReady_ShouldAddUserToReadySet_WhenUserMarkedAsReady() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);

        boolean isReadyResult = readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(isReadyResult).isTrue();
    }

    @Test
    @DisplayName("Отметка готовности нескольких пользователей сохраняет всех в множество")
    void markReady_ShouldAddMultipleUsersToReadySet_WhenMultipleUsersMarkedAsReady() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_THIRD);

        Set<Long> idsParticipantReady = readyService.getReadyParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipantReady).containsExactlyInAnyOrder(ID_USER_FIRST, ID_USER_SECOND, ID_USER_THIRD);
    }

    @Test
    @DisplayName("Снятие готовности удаляет пользователя из множества готовых")
    void unmarkReady_ShouldRemoveUserFromReadySet_WhenUserUnmarked() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isTrue();

        readyService.unmarkReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isFalse();
    }

    @Test
    @DisplayName("Снятие готовности одного пользователя не влияет на остальных")
    void unmarkReady_ShouldNotAffectOtherUsers_WhenOneUserUnmarked() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);

        readyService.unmarkReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isFalse();
        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_SECOND)).isTrue();
    }

    @Test
    @DisplayName("Проверка готовности возвращает false для неготового пользователя")
    void isReady_ShouldReturnFalse_WhenUserNotMarkedAsReady() {
        boolean isReadyResult = readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(isReadyResult).isFalse();
    }

    @Test
    @DisplayName("Проверка готовности возвращает true для готового пользователя среди нескольких")
    void isReady_ShouldReturnTrue_WhenUserMarkedAsReadyAmongOthers() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);

        boolean isReadyResultFirst = readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST);
        boolean isReadyResultSecond = readyService.isReady(ID_CONTEST_TEST, ID_USER_SECOND);
        boolean isReadyResultThird = readyService.isReady(ID_CONTEST_TEST, ID_USER_THIRD);

        assertThat(isReadyResultFirst).isTrue();
        assertThat(isReadyResultSecond).isTrue();
        assertThat(isReadyResultThird).isFalse();
    }

    @Test
    @DisplayName("Получение идентификаторов готовых участников возвращает пустое множество когда никто не готов")
    void getReadyParticipantIds_ShouldReturnEmptySet_WhenNoUsersReady() {
        Set<Long> idsParticipantReady = readyService.getReadyParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipantReady).isEmpty();
    }

    @Test
    @DisplayName("Получение идентификаторов готовых участников возвращает всех готовых пользователей")
    void getReadyParticipantIds_ShouldReturnAllReadyUsers_WhenMultipleUsersReady() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);

        Set<Long> idsParticipantReady = readyService.getReadyParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipantReady)
                .hasSize(2)
                .containsExactlyInAnyOrder(ID_USER_FIRST, ID_USER_SECOND);
    }

    @Test
    @DisplayName("Подсчёт готовых участников возвращает ноль когда никто не готов")
    void getReadyCount_ShouldReturnZero_WhenNoUsersReady() {
        int countReady = readyService.getReadyCount(ID_CONTEST_TEST);

        assertThat(countReady).isZero();
    }

    @Test
    @DisplayName("Подсчёт готовых участников возвращает корректное количество")
    void getReadyCount_ShouldReturnCorrectCount_WhenMultipleUsersReady() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_THIRD);

        int countReady = readyService.getReadyCount(ID_CONTEST_TEST);

        assertThat(countReady).isEqualTo(3);
    }

    @Test
    @DisplayName("Очистка готовности удаляет всех готовых участников")
    void clearReady_ShouldRemoveAllReadyUsers_WhenReadySetCleared() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);
        assertThat(readyService.getReadyCount(ID_CONTEST_TEST)).isEqualTo(2);

        readyService.clearReady(ID_CONTEST_TEST);

        assertThat(readyService.getReadyCount(ID_CONTEST_TEST)).isZero();
        assertThat(readyService.getReadyParticipantIds(ID_CONTEST_TEST)).isEmpty();
    }

    @Test
    @DisplayName("Очистка готовности не вызывает ошибку для пустого множества")
    void clearReady_ShouldNotThrowException_WhenReadySetIsEmpty() {
        readyService.clearReady(ID_CONTEST_TEST);

        assertThat(readyService.getReadyCount(ID_CONTEST_TEST)).isZero();
    }

    @Test
    @DisplayName("Повторная отметка готовности не дублирует пользователя")
    void markReady_ShouldNotDuplicateUser_WhenUserMarkedReadyTwice() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);

        int countReady = readyService.getReadyCount(ID_CONTEST_TEST);

        assertThat(countReady).isEqualTo(1);
    }

    @Test
    @DisplayName("Снятие готовности несуществующего пользователя не вызывает ошибку")
    void unmarkReady_ShouldNotThrowException_WhenUserNotInReadySet() {
        readyService.unmarkReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isFalse();
    }

    @Test
    @DisplayName("Готовность участников изолирована между разными соревнованиями")
    void markReady_ShouldIsolateReadyStateByContest_WhenDifferentContests() {
        Long idContestOther = 999L;
        roomService.createRoom(idContestOther, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(idContestOther, ID_USER_SECOND);

        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isTrue();
        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_SECOND)).isFalse();
        assertThat(readyService.isReady(idContestOther, ID_USER_FIRST)).isFalse();
        assertThat(readyService.isReady(idContestOther, ID_USER_SECOND)).isTrue();

        redisTemplate.delete(keyReady(idContestOther));
        roomService.deleteRoom(idContestOther);
    }

    @Test
    @DisplayName("Подсчёт готовых после снятия готовности возвращает актуальное значение")
    void getReadyCount_ShouldReturnUpdatedCount_WhenUserUnmarked() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);
        assertThat(readyService.getReadyCount(ID_CONTEST_TEST)).isEqualTo(2);

        readyService.unmarkReady(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(readyService.getReadyCount(ID_CONTEST_TEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Получение идентификаторов после очистки возвращает пустое множество")
    void getReadyParticipantIds_ShouldReturnEmptySet_WhenReadyCleared() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        readyService.markReady(ID_CONTEST_TEST, ID_USER_SECOND);
        readyService.clearReady(ID_CONTEST_TEST);

        Set<Long> idsParticipantReady = readyService.getReadyParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipantReady).isEmpty();
    }

    @Test
    @DisplayName("Проверка готовности возвращает false после очистки")
    void isReady_ShouldReturnFalse_WhenReadyCleared() {
        readyService.markReady(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isTrue();

        readyService.clearReady(ID_CONTEST_TEST);

        assertThat(readyService.isReady(ID_CONTEST_TEST, ID_USER_FIRST)).isFalse();
    }
}
