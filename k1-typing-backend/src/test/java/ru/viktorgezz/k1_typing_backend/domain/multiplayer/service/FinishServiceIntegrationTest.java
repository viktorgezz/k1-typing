package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.FinishService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import testconfig.AbstractIntegrationRedisTest;

import static org.assertj.core.api.Assertions.assertThat;

class FinishServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private FinishService finishService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private RoomService roomService;

    private static final Long ID_CONTEST_TEST = 4L;
    private static final Long ID_EXERCISE_TEST = 100L;
    private static final int PARTICIPANTS_MAX_TEST = 4;

    private static final Long ID_USER_FIRST = 50L;
    private static final Long ID_USER_SECOND = 60L;
    private static final Long ID_USER_THIRD = 70L;
    private static final Long ID_USER_FOURTH = 80L;
    private static final String USERNAME_FIRST = "RacerFirst";
    private static final String USERNAME_SECOND = "RacerSecond";
    private static final String USERNAME_THIRD = "RacerThird";
    private static final String USERNAME_FOURTH = "RacerFourth";

    @BeforeEach
    void setupRoomWithParticipants() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_THIRD, USERNAME_THIRD);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FOURTH, USERNAME_FOURTH);
    }

    @AfterEach
    void cleanupRedis() {
        roomService.deleteRoom(ID_CONTEST_TEST);
    }

    @Test
    @DisplayName("Первый финишировавший участник получает первое место")
    void registerFinish_ShouldReturnFirstPlace_WhenUserFinishesFirst() {
        Place placeResult = finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(placeResult).isEqualTo(Place.FIRST);
    }

    @Test
    @DisplayName("Второй финишировавший участник получает второе место")
    void registerFinish_ShouldReturnSecondPlace_WhenUserFinishesSecond() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);

        Place placeResult = finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);

        assertThat(placeResult).isEqualTo(Place.SECOND);
    }

    @Test
    @DisplayName("Третий финишировавший участник получает третье место")
    void registerFinish_ShouldReturnThirdPlace_WhenUserFinishesThird() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);

        Place placeResult = finishService.registerFinish(ID_CONTEST_TEST, ID_USER_THIRD);

        assertThat(placeResult).isEqualTo(Place.THIRD);
    }

    @Test
    @DisplayName("Четвёртый и последующие участники получают статус без места")
    void registerFinish_ShouldReturnWithoutPlace_WhenUserFinishesFourthOrLater() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_THIRD);

        Place placeResult = finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FOURTH);

        assertThat(placeResult).isEqualTo(Place.WITHOUT_PLACE);
    }

    @Test
    @DisplayName("Подсчёт финишировавших возвращает корректное количество")
    void getFinishersCount_ShouldReturnCorrectCount_WhenMultipleUsersFinished() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);

        int countFinishers = finishService.getFinishersCount(ID_CONTEST_TEST);

        assertThat(countFinishers).isEqualTo(2);
    }

    @Test
    @DisplayName("Подсчёт финишировавших возвращает ноль когда никто не финишировал")
    void getFinishersCount_ShouldReturnZero_WhenNoUsersFinished() {
        int countFinishers = finishService.getFinishersCount(ID_CONTEST_TEST);

        assertThat(countFinishers).isZero();
    }

    @Test
    @DisplayName("Проверка завершённости соревнования возвращает false когда не все финишировали")
    void isContestComplete_ShouldReturnFalse_WhenNotAllParticipantsFinished() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);

        boolean isContestCompleteResult = finishService.isContestComplete(ID_CONTEST_TEST);

        assertThat(isContestCompleteResult).isFalse();
    }

    @Test
    @DisplayName("Проверка завершённости соревнования возвращает true когда все финишировали")
    void isContestComplete_ShouldReturnTrue_WhenAllParticipantsFinished() {
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FIRST);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_SECOND);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_THIRD);
        finishService.registerFinish(ID_CONTEST_TEST, ID_USER_FOURTH);

        boolean isContestCompleteResult = finishService.isContestComplete(ID_CONTEST_TEST);

        assertThat(isContestCompleteResult).isTrue();
    }

    @Test
    @DisplayName("Проверка завершённости возвращает false для пустой комнаты без участников")
    void isContestComplete_ShouldReturnFalse_WhenRoomHasNoParticipants() {
        Long idContestEmpty = 999L;
        roomService.createRoom(idContestEmpty, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        boolean isContestCompleteResult = finishService.isContestComplete(idContestEmpty);

        assertThat(isContestCompleteResult).isFalse();

        roomService.deleteRoom(idContestEmpty);
    }
}
