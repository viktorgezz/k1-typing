package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.AllProgressMessage.UserProgressData;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ProgressService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import testconfig.AbstractIntegrationRedisTest;

class ProgressServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private RoomService roomService;

    private static final Long ID_CONTEST_TEST = 3L;
    private static final Long ID_EXERCISE_TEST = 100L;
    private static final int PARTICIPANTS_MAX_TEST = 4;

    private static final Long ID_USER_FIRST = 30L;
    private static final Long ID_USER_SECOND = 40L;
    private static final String USERNAME_FIRST = "TyperFirst";
    private static final String USERNAME_SECOND = "TyperSecond";

    @BeforeEach
    void setupRoomWithParticipants() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);
    }

    @AfterEach
    void cleanupRedis() {
        roomService.deleteRoom(ID_CONTEST_TEST);
    }

    @Test
    @DisplayName("Обновление прогресса сохраняет новое значение для пользователя")
    void updateProgress_ShouldStoreNewProgressValue_WhenValidDataProvided() {
        int progressPercentNew = 50;
        int speedNew = 200;
        BigDecimal accuracyNew = new BigDecimal("95.50");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentNew, speedNew, accuracyNew);

        UserProgressData progressDataStored = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(progressDataStored.progress()).isEqualTo(progressPercentNew);
        assertThat(progressDataStored.speed()).isEqualTo(speedNew);
        assertThat(progressDataStored.accuracy()).isEqualByComparingTo(accuracyNew);
    }

    @Test
    @DisplayName("Обновление прогресса перезаписывает предыдущее значение")
    void updateProgress_ShouldOverwritePreviousValue_WhenProgressUpdatedAgain() {
        int progressPercentInitial = 30;
        int speedInitial = 150;
        BigDecimal accuracyInitial = new BigDecimal("90.00");
        int progressPercentUpdated = 75;
        int speedUpdated = 250;
        BigDecimal accuracyUpdated = new BigDecimal("98.00");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentInitial, speedInitial, accuracyInitial);
        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentUpdated, speedUpdated, accuracyUpdated);

        UserProgressData progressDataStored = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(progressDataStored.progress()).isEqualTo(progressPercentUpdated);
        assertThat(progressDataStored.speed()).isEqualTo(speedUpdated);
        assertThat(progressDataStored.accuracy()).isEqualByComparingTo(accuracyUpdated);
    }

    @Test
    @DisplayName("Получение прогресса всех участников возвращает корректную карту")
    void getProgressAll_ShouldReturnMapWithAllParticipants_WhenMultipleUsersExist() {
        int progressPercentFirst = 25;
        int speedFirst = 180;
        BigDecimal accuracyFirst = new BigDecimal("92.00");
        int progressPercentSecond = 60;
        int speedSecond = 220;
        BigDecimal accuracySecond = new BigDecimal("95.50");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentFirst, speedFirst, accuracyFirst);
        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_SECOND, progressPercentSecond, speedSecond, accuracySecond);

        Map<Long, UserProgressData> progressAll = progressService.getProgressAll(ID_CONTEST_TEST);

        assertThat(progressAll).hasSize(2);
        assertThat(progressAll.get(ID_USER_FIRST).progress()).isEqualTo(progressPercentFirst);
        assertThat(progressAll.get(ID_USER_FIRST).speed()).isEqualTo(speedFirst);
        assertThat(progressAll.get(ID_USER_FIRST).accuracy()).isEqualByComparingTo(accuracyFirst);
        assertThat(progressAll.get(ID_USER_SECOND).progress()).isEqualTo(progressPercentSecond);
        assertThat(progressAll.get(ID_USER_SECOND).speed()).isEqualTo(speedSecond);
        assertThat(progressAll.get(ID_USER_SECOND).accuracy()).isEqualByComparingTo(accuracySecond);
    }

    @Test
    @DisplayName("Получение прогресса пользователя возвращает начальное нулевое значение")
    void getProgressByUser_ShouldReturnZero_WhenProgressWasNotUpdated() {
        UserProgressData progressDataStored = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(progressDataStored.progress()).isZero();
        assertThat(progressDataStored.speed()).isZero();
        assertThat(progressDataStored.accuracy()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Получение прогресса пользователя возвращает актуальное значение после обновления")
    void getProgressByUser_ShouldReturnUpdatedValue_WhenProgressWasModified() {
        int progressPercentExpected = 88;
        int speedExpected = 240;
        BigDecimal accuracyExpected = new BigDecimal("97.50");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentExpected, speedExpected, accuracyExpected);

        UserProgressData progressDataActual = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(progressDataActual.progress()).isEqualTo(progressPercentExpected);
        assertThat(progressDataActual.speed()).isEqualTo(speedExpected);
        assertThat(progressDataActual.accuracy()).isEqualByComparingTo(accuracyExpected);
    }

    @Test
    @DisplayName("Прогресс разных пользователей хранится независимо")
    void updateProgress_ShouldStoreProgressIndependently_WhenMultipleUsersUpdateProgress() {
        int progressPercentFirst = 45;
        int speedFirst = 200;
        BigDecimal accuracyFirst = new BigDecimal("93.00");
        int progressPercentSecond = 90;
        int speedSecond = 280;
        BigDecimal accuracySecond = new BigDecimal("99.00");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentFirst, speedFirst, accuracyFirst);
        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_SECOND, progressPercentSecond, speedSecond, accuracySecond);

        UserProgressData progressDataFirst = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);
        UserProgressData progressDataSecond = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_SECOND);

        assertThat(progressDataFirst.progress()).isEqualTo(progressPercentFirst);
        assertThat(progressDataFirst.speed()).isEqualTo(speedFirst);
        assertThat(progressDataFirst.accuracy()).isEqualByComparingTo(accuracyFirst);
        assertThat(progressDataSecond.progress()).isEqualTo(progressPercentSecond);
        assertThat(progressDataSecond.speed()).isEqualTo(speedSecond);
        assertThat(progressDataSecond.accuracy()).isEqualByComparingTo(accuracySecond);
    }

    @Test
    @DisplayName("Обновление прогресса до 100% сохраняется корректно")
    void updateProgress_ShouldStoreHundredPercent_WhenUserCompletesTyping() {
        int progressPercentComplete = 100;
        int speedComplete = 300;
        BigDecimal accuracyComplete = new BigDecimal("100.00");

        progressService.updateProgress(ID_CONTEST_TEST, ID_USER_FIRST, progressPercentComplete, speedComplete, accuracyComplete);

        UserProgressData progressDataStored = progressService.getProgressByUser(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(progressDataStored.progress()).isEqualTo(progressPercentComplete);
        assertThat(progressDataStored.speed()).isEqualTo(speedComplete);
        assertThat(progressDataStored.accuracy()).isEqualByComparingTo(accuracyComplete);
    }
}
