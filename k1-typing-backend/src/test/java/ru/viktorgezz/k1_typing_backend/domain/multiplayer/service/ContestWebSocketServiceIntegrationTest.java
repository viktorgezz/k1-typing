package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.TOPIC_FINISHED;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.TOPIC_PLAYER_FINISHED;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.TOPIC_PLAYER_JOINED;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.TOPIC_PLAYER_LEFT;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.TOPIC_PROGRESS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.repo.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.AllProgressMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ContestFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.FinishMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerJoinedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerLeftRoomMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ProgressUpdateMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ProgressService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ReadyService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf.ContestWebSocketService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import testconfig.AbstractIntegrationRedisTest;

class ContestWebSocketServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private ContestWebSocketService contestWebSocketService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ReadyService readyService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ResultItemRepo resultItemRepo;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private Contest contestSaved;
    private User userFirst;
    private User userSecond;
    private User userThird;
    private Exercise exerciseSaved;

    private static final int PARTICIPANTS_MAX_TEST = 4;
    private static final String USERNAME_FIRST = "PlayerAlpha";
    private static final String USERNAME_SECOND = "PlayerBeta";
    private static final String USERNAME_THIRD = "PlayerGamma";
    private static final String PASSWORD_TEST = "password123";
    private static final String EXERCISE_TITLE_TEST = "Test Exercise";
    private static final String EXERCISE_TEXT_TEST = "The quick brown fox jumps over the lazy dog.";

    @BeforeEach
    void setupTestData() {
        userFirst = userRepo.save(new User(USERNAME_FIRST, PASSWORD_TEST, Role.USER, true, false, false));
        userSecond = userRepo.save(new User(USERNAME_SECOND, PASSWORD_TEST, Role.USER, true, false, false));
        userThird = userRepo.save(new User(USERNAME_THIRD, PASSWORD_TEST, Role.USER, true, false, false));

        exerciseSaved = exerciseRepo.save(new Exercise(EXERCISE_TITLE_TEST, EXERCISE_TEXT_TEST, userFirst));

        contestSaved = contestRepo.save(new Contest(Status.CREATED, PARTICIPANTS_MAX_TEST, exerciseSaved));

        roomService.createRoom(contestSaved.getId(), exerciseSaved.getId(), PARTICIPANTS_MAX_TEST);
        participantsService.addParticipant(contestSaved.getId(), userFirst.getId(), USERNAME_FIRST);
        participantsService.addParticipant(contestSaved.getId(), userSecond.getId(), USERNAME_SECOND);
    }

    @AfterEach
    void cleanupTestData() {
        roomService.deleteRoom(contestSaved.getId());
        resultItemRepo.deleteAll();
        contestRepo.deleteAll();
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
        reset(messagingTemplate);
    }

    @Test
    @DisplayName("Обработка прогресса рассылает обновлённый прогресс всех участников")
    void processProgress_ShouldBroadcastAllParticipantsProgress_WhenProgressUpdated() {
        int progressPercentExpected = 50;
        int speedExpected = 200;
        BigDecimal accuracyExpected = new BigDecimal("95.50");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercentExpected, speedExpected, accuracyExpected);

        contestWebSocketService.processProgress(contestSaved.getId(), userFirst.getId(), messageProgress);

        ArgumentCaptor<AllProgressMessage> messageCaptor = ArgumentCaptor.forClass(AllProgressMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_PROGRESS, contestSaved.getId())),
                messageCaptor.capture()
        );

        AllProgressMessage messageCaptured = messageCaptor.getValue();
        assertThat(messageCaptured.usersProgress()).containsKey(userFirst.getId());
        AllProgressMessage.UserProgressData progressData = messageCaptured.usersProgress().get(userFirst.getId());
        assertThat(progressData.progress()).isEqualTo(progressPercentExpected);
        assertThat(progressData.speed()).isEqualTo(speedExpected);
        assertThat(progressData.accuracy()).isEqualByComparingTo(accuracyExpected);
    }

    @Test
    @DisplayName("Обработка прогресса сохраняет значение в Redis")
    void processProgress_ShouldStoreProgressInRedis_WhenProgressUpdated() {
        int progressPercentExpected = 75;
        int speedExpected = 220;
        BigDecimal accuracyExpected = new BigDecimal("96.00");
        ProgressUpdateMessage messageProgress = new ProgressUpdateMessage(progressPercentExpected, speedExpected, accuracyExpected);

        contestWebSocketService.processProgress(contestSaved.getId(), userFirst.getId(), messageProgress);

        Map<Long, AllProgressMessage.UserProgressData> progressAll = progressService.getProgressAll(contestSaved.getId());
        assertThat(progressAll).containsKey(userFirst.getId());
        AllProgressMessage.UserProgressData progressData = progressAll.get(userFirst.getId());
        assertThat(progressData.progress()).isEqualTo(progressPercentExpected);
        assertThat(progressData.speed()).isEqualTo(speedExpected);
        assertThat(progressData.accuracy()).isEqualByComparingTo(accuracyExpected);
    }

    @Test
    @DisplayName("Транзакционная обработка финиша возвращает корректное сообщение с первым местом")
    void processFinishTransaction_ShouldReturnMessageWithFirstPlace_WhenUserFinishesFirst() {
        FinishMessage messageFinish = new FinishMessage(120L, 200, new BigDecimal("95.50"));

        PlayerFinishedMessage messageResult = contestWebSocketService.processFinishTransaction(
                contestSaved.getId(), userFirst.getId(), messageFinish
        );

        assertThat(messageResult.idUser()).isEqualTo(userFirst.getId());
        assertThat(messageResult.username()).isEqualTo(USERNAME_FIRST);
        assertThat(messageResult.place()).isEqualTo(Place.FIRST);
        assertThat(messageResult.speed()).isEqualTo(200);
        assertThat(messageResult.durationSeconds()).isEqualTo(120L);
        assertThat(messageResult.accuracy()).isEqualByComparingTo(new BigDecimal("95.50"));
    }

    @Test
    @DisplayName("Транзакционная обработка финиша сохраняет результат в базу данных")
    void processFinishTransaction_ShouldSaveResultToDatabase_WhenUserFinishes() {
        FinishMessage messageFinish = new FinishMessage(90L, 180, new BigDecimal("98.00"));

        contestWebSocketService.processFinishTransaction(contestSaved.getId(), userFirst.getId(), messageFinish);

        List<ResultItem> resultsFound = resultItemRepo.findAllByContestIdOrderByPlaceAsc(contestSaved.getId());
        assertThat(resultsFound).hasSize(1);
        ResultItem resultItem = resultsFound.get(0);
        assertThat(resultItem.getUser().getId()).isEqualTo(userFirst.getId());
        assertThat(resultItem.getSpeed()).isEqualTo(180);
        assertThat(resultItem.getDurationSeconds()).isEqualTo(90L);
        assertThat(resultItem.getAccuracy()).isEqualByComparingTo(new BigDecimal("98.00"));
        assertThat(resultItem.getPlace()).isEqualTo(Place.FIRST);
    }

    @Test
    @DisplayName("Транзакционная обработка финиша назначает места в порядке финиширования")
    void processFinishTransaction_ShouldAssignPlacesInOrder_WhenMultipleUsersFinish() {
        FinishMessage messageFinishFirst = new FinishMessage(60L, 220, new BigDecimal("99.00"));
        FinishMessage messageFinishSecond = new FinishMessage(70L, 190, new BigDecimal("97.50"));

        PlayerFinishedMessage messageResultFirst = contestWebSocketService.processFinishTransaction(
                contestSaved.getId(), userSecond.getId(), messageFinishFirst
        );
        PlayerFinishedMessage messageResultSecond = contestWebSocketService.processFinishTransaction(
                contestSaved.getId(), userFirst.getId(), messageFinishSecond
        );

        assertThat(messageResultFirst.place()).isEqualTo(Place.FIRST);
        assertThat(messageResultSecond.place()).isEqualTo(Place.SECOND);
    }

    @Test
    @DisplayName("Обработка финиша рассылает сообщение о завершении игрока")
    void processFinish_ShouldBroadcastPlayerFinishedMessage_WhenUserFinishes() {
        FinishMessage messageFinish = new FinishMessage(100L, 150, new BigDecimal("92.00"));

        contestWebSocketService.processFinish(contestSaved.getId(), userFirst.getId(), messageFinish);

        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_PLAYER_FINISHED, contestSaved.getId())),
                any(PlayerFinishedMessage.class)
        );
    }

    @Test
    @DisplayName("Обработка готовности отмечает участника как готового")
    void processReady_ShouldMarkParticipantAsReady_WhenUserBecomesReady() {
        contestWebSocketService.processReady(contestSaved.getId(), userFirst.getId());

        boolean isReadyResult = readyService.isReady(contestSaved.getId(), userFirst.getId());
        assertThat(isReadyResult).isTrue();
    }

    @Test
    @DisplayName("Обработка готовности изменяет статус на WAITING когда все участники готовы")
    void processReady_ShouldChangeStatusToWaiting_WhenAllParticipantsReady() {
        contestWebSocketService.processReady(contestSaved.getId(), userFirst.getId());
        contestWebSocketService.processReady(contestSaved.getId(), userSecond.getId());

        Contest contestUpdated = contestRepo.findById(contestSaved.getId()).orElseThrow();
        assertThat(contestUpdated.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("Обработка готовности не изменяет статус когда готов только один участник")
    void processReady_ShouldNotChangeStatus_WhenOnlyOneParticipantReady() {
        contestWebSocketService.processReady(contestSaved.getId(), userFirst.getId());

        Contest contestUpdated = contestRepo.findById(contestSaved.getId()).orElseThrow();
        assertThat(contestUpdated.getStatus()).isEqualTo(Status.CREATED);
    }

    @Test
    @DisplayName("Рассылка присоединения игрока отправляет корректное сообщение")
    void broadcastPlayerJoined_ShouldSendCorrectMessage_WhenPlayerJoins() {
        contestWebSocketService.broadcastPlayerJoined(contestSaved.getId(), userFirst.getId(), USERNAME_FIRST);

        ArgumentCaptor<PlayerJoinedMessage> messageCaptor = ArgumentCaptor.forClass(PlayerJoinedMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_PLAYER_JOINED, contestSaved.getId())),
                messageCaptor.capture()
        );

        PlayerJoinedMessage messageCaptured = messageCaptor.getValue();
        assertThat(messageCaptured.userId()).isEqualTo(userFirst.getId());
        assertThat(messageCaptured.userName()).isEqualTo(USERNAME_FIRST);
        assertThat(messageCaptured.currentPlayers()).isEqualTo(2);
        assertThat(messageCaptured.maxPlayers()).isEqualTo(PARTICIPANTS_MAX_TEST);
    }

    @Test
    @DisplayName("Рассылка выхода игрока отправляет корректное сообщение")
    void broadcastPlayerLeft_ShouldSendCorrectMessage_WhenPlayerLeaves() {
        contestWebSocketService.broadcastPlayerLeft(contestSaved.getId(), userFirst.getId(), USERNAME_FIRST);

        ArgumentCaptor<PlayerLeftRoomMessage> messageCaptor = ArgumentCaptor.forClass(PlayerLeftRoomMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_PLAYER_LEFT, contestSaved.getId())),
                messageCaptor.capture()
        );

        PlayerLeftRoomMessage messageCaptured = messageCaptor.getValue();
        assertThat(messageCaptured.idUser()).isEqualTo(userFirst.getId());
        assertThat(messageCaptured.username()).isEqualTo(USERNAME_FIRST);
    }

    @Test
    @DisplayName("Рассылка выхода игрока снимает его готовность")
    void broadcastPlayerLeft_ShouldUnmarkUserReady_WhenPlayerLeaves() {
        readyService.markReady(contestSaved.getId(), userFirst.getId());
        assertThat(readyService.isReady(contestSaved.getId(), userFirst.getId())).isTrue();

        contestWebSocketService.broadcastPlayerLeft(contestSaved.getId(), userFirst.getId(), USERNAME_FIRST);

        assertThat(readyService.isReady(contestSaved.getId(), userFirst.getId())).isFalse();
    }

    @Test
    @DisplayName("Транзакционное завершение соревнования устанавливает статус FINISHED")
    void finishContestTransaction_ShouldSetStatusFinished_WhenContestEnds() {
        contestWebSocketService.finishContestTransaction(contestSaved.getId());

        Contest contestUpdated = contestRepo.findById(contestSaved.getId()).orElseThrow();
        assertThat(contestUpdated.getStatus()).isEqualTo(Status.FINISHED);
    }

    @Test
    @DisplayName("Транзакционное завершение возвращает лидерборд с результатами")
    void finishContestTransaction_ShouldReturnLeaderboardWithResults_WhenResultsExist() {
        FinishMessage messageFinish = new FinishMessage(80L, 210, new BigDecimal("96.50"));
        contestWebSocketService.processFinishTransaction(contestSaved.getId(), userFirst.getId(), messageFinish);

        ContestFinishedMessage messageResult = contestWebSocketService.finishContestTransaction(contestSaved.getId());

        assertThat(messageResult.leaderboard()).hasSize(1);
        ContestFinishedMessage.LeaderboardEntry entryFirst = messageResult.leaderboard().get(0);
        assertThat(entryFirst.idUser()).isEqualTo(userFirst.getId());
        assertThat(entryFirst.place()).isEqualTo(Place.FIRST);
    }

    @Test
    @DisplayName("Транзакционное завершение возвращает пустой лидерборд когда нет результатов")
    void finishContestTransaction_ShouldReturnEmptyLeaderboard_WhenNoResultsExist() {
        ContestFinishedMessage messageResult = contestWebSocketService.finishContestTransaction(contestSaved.getId());

        assertThat(messageResult.leaderboard()).isEmpty();
    }

    @Test
    @DisplayName("Завершение соревнования рассылает сообщение о завершении")
    void finishContest_ShouldBroadcastContestFinishedMessage_WhenContestEnds() {
        contestWebSocketService.finishContest(contestSaved.getId());

        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_FINISHED, contestSaved.getId())),
                any(ContestFinishedMessage.class)
        );
    }

    @Test
    @DisplayName("Транзакционное завершение формирует лидерборд с корректными именами участников")
    void finishContestTransaction_ShouldBuildLeaderboardWithCorrectUsernames_WhenMultipleResultsExist() {
        FinishMessage messageFinishFirst = new FinishMessage(50L, 250, new BigDecimal("100.00"));
        FinishMessage messageFinishSecond = new FinishMessage(60L, 200, new BigDecimal("95.00"));

        contestWebSocketService.processFinishTransaction(contestSaved.getId(), userFirst.getId(), messageFinishFirst);
        contestWebSocketService.processFinishTransaction(contestSaved.getId(), userSecond.getId(), messageFinishSecond);

        ContestFinishedMessage messageResult = contestWebSocketService.finishContestTransaction(contestSaved.getId());

        assertThat(messageResult.leaderboard()).hasSize(2);
        assertThat(messageResult.leaderboard())
                .extracting(ContestFinishedMessage.LeaderboardEntry::username)
                .containsExactlyInAnyOrder(USERNAME_FIRST, USERNAME_SECOND);
    }

    @Test
    @DisplayName("Обработка прогресса нескольких пользователей сохраняет все значения независимо")
    void processProgress_ShouldStoreProgressIndependently_WhenMultipleUsersUpdateProgress() {
        int progressPercentFirst = 30;
        int speedFirst = 180;
        BigDecimal accuracyFirst = new BigDecimal("92.00");
        int progressPercentSecond = 70;
        int speedSecond = 240;
        BigDecimal accuracySecond = new BigDecimal("96.50");

        contestWebSocketService.processProgress(
                contestSaved.getId(), userFirst.getId(), new ProgressUpdateMessage(progressPercentFirst, speedFirst, accuracyFirst)
        );
        contestWebSocketService.processProgress(
                contestSaved.getId(), userSecond.getId(), new ProgressUpdateMessage(progressPercentSecond, speedSecond, accuracySecond)
        );

        verify(messagingTemplate, times(2)).convertAndSend(
                eq(String.format(TOPIC_PROGRESS, contestSaved.getId())),
                any(AllProgressMessage.class)
        );
    }

    @Test
    @DisplayName("Присоединение третьего игрока показывает корректное количество участников")
    void broadcastPlayerJoined_ShouldShowCorrectParticipantCount_WhenThirdPlayerJoins() {
        participantsService.addParticipant(contestSaved.getId(), userThird.getId(), USERNAME_THIRD);

        contestWebSocketService.broadcastPlayerJoined(contestSaved.getId(), userThird.getId(), USERNAME_THIRD);

        ArgumentCaptor<PlayerJoinedMessage> messageCaptor = ArgumentCaptor.forClass(PlayerJoinedMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq(String.format(TOPIC_PLAYER_JOINED, contestSaved.getId())),
                messageCaptor.capture()
        );

        PlayerJoinedMessage messageCaptured = messageCaptor.getValue();
        assertThat(messageCaptured.currentPlayers()).isEqualTo(3);
    }

    @Test
    @DisplayName("Финиш после удаления участника присваивает место на основании оставшихся")
    void processFinishTransaction_ShouldAssignPlaceBasedOnRemainingParticipants_WhenUserFinishesAfterSomeoneLeft() {
        participantsService.removeParticipant(contestSaved.getId(), userSecond.getId());
        FinishMessage messageFinish = new FinishMessage(100L, 180, new BigDecimal("90.00"));

        PlayerFinishedMessage messageResult = contestWebSocketService.processFinishTransaction(
                contestSaved.getId(), userFirst.getId(), messageFinish
        );

        assertThat(messageResult.place()).isEqualTo(Place.FIRST);
    }
}
