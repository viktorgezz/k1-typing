package ru.viktorgezz.k1_typing_backend.domain.multiplayer.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompSession;

import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationResponse;
import ru.viktorgezz.k1_typing_backend.auth.dto.RegistrationRequest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.repo.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rq.CreateRoomRqDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.JoinRoomRsDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ContestFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ContestStartMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.CountdownMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.FinishMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerJoinedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ProgressUpdateMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.security.repo.RefreshTokenRepo;
import testconfig.AbstractWebSocketE2ETest;

class ContestFullScenarioE2ETest extends AbstractWebSocketE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ResultItemRepo resultItemRepo;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    private String tokenPlayerFirst;
    private String tokenPlayerSecond;
    private Long idContestCreated;
    private Exercise exerciseSaved;

    private static final String USERNAME_FIRST = "PlayerFirstE2E";
    private static final String USERNAME_SECOND = "PlayerSecondE2E";
    private static final String PASSWORD_TEST = "Password123!";
    private static final String EXERCISE_TEXT_TEST = "The quick brown fox jumps over the lazy dog";

    @BeforeEach
    void setupPlayersAndExercise() {
        setupStompClient();

        tokenPlayerFirst = registerAndLogin(USERNAME_FIRST, PASSWORD_TEST);
        tokenPlayerSecond = registerAndLogin(USERNAME_SECOND, PASSWORD_TEST);

        exerciseSaved = createExercise();
    }

    @AfterEach
    void cleanupTestData() {
        if (idContestCreated != null) {
            roomService.deleteRoom(idContestCreated);
        }
        resultItemRepo.deleteAll();
        contestRepo.deleteAll();
        exerciseRepo.deleteAll();
        refreshTokenRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Полный сценарий соревнования от создания комнаты до финиша с формированием лидерборда")
    void fullContestScenario_ShouldCompleteSuccessfully_WhenTwoPlayersParticipate() throws Exception {
        JoinRoomRsDto responseCreate = createRoom(tokenPlayerFirst, exerciseSaved.getId(), 2);
        idContestCreated = responseCreate.idContest();
        assertThat(responseCreate.status()).isEqualTo(JoinRoomRsDto.JoinRoomStatus.SUCCESS);

        JoinRoomRsDto responseJoin = joinRoom(tokenPlayerSecond, idContestCreated);
        assertThat(responseJoin.status()).isEqualTo(JoinRoomRsDto.JoinRoomStatus.SUCCESS);

        StompSession sessionFirst = connectToWebSocket(tokenPlayerFirst);
        StompSession sessionSecond = connectToWebSocket(tokenPlayerSecond);

        String topicCountdown = String.format("/topic/contest/%d/countdown", idContestCreated);
        String topicStart = String.format("/topic/contest/%d/start", idContestCreated);
        String topicPlayerFinished = String.format("/topic/contest/%d/player-finished", idContestCreated);
        String topicFinished = String.format("/topic/contest/%d/finished", idContestCreated);

        BlockingQueue<CountdownMessage> countdownQueueFirst = subscribeToTopic(sessionFirst, topicCountdown, CountdownMessage.class);
        BlockingQueue<ContestStartMessage> startQueueFirst = subscribeToTopic(sessionFirst, topicStart, ContestStartMessage.class);
        BlockingQueue<PlayerFinishedMessage> playerFinishedQueueFirst = subscribeToTopic(sessionFirst, topicPlayerFinished, PlayerFinishedMessage.class);
        BlockingQueue<ContestFinishedMessage> finishedQueueFirst = subscribeToTopic(sessionFirst, topicFinished, ContestFinishedMessage.class);

        subscribeToTopic(sessionSecond, topicCountdown, CountdownMessage.class);

        Thread.sleep(500);

        String destinationReady = String.format("/app/contest/%d/ready", idContestCreated);
        sessionFirst.send(destinationReady, null);
        sessionSecond.send(destinationReady, null);

        CountdownMessage countdownMessageFirst = countdownQueueFirst.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(countdownMessageFirst).isNotNull();
        assertThat(countdownMessageFirst.seconds()).isLessThanOrEqualTo(5);

        ContestStartMessage startMessage = startQueueFirst.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(startMessage).isNotNull();
        assertThat(startMessage.text()).isEqualTo(EXERCISE_TEXT_TEST);

        String destinationProgress = String.format("/app/contest/%d/progress", idContestCreated);
        sessionFirst.send(destinationProgress, new ProgressUpdateMessage(50, 200, new BigDecimal("95.50")));
        sessionSecond.send(destinationProgress, new ProgressUpdateMessage(30, 150, new BigDecimal("90.00")));

        Thread.sleep(200);

        String destinationFinish = String.format("/app/contest/%d/finish", idContestCreated);
        FinishMessage finishMessageFirst = new FinishMessage(60L, 200, new BigDecimal("98.50"));
        FinishMessage finishMessageSecond = new FinishMessage(70L, 180, new BigDecimal("95.00"));

        sessionFirst.send(destinationFinish, finishMessageFirst);
        Thread.sleep(100);
        sessionSecond.send(destinationFinish, finishMessageSecond);

        PlayerFinishedMessage playerFinishedFirst = playerFinishedQueueFirst.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(playerFinishedFirst).isNotNull();
        assertThat(playerFinishedFirst.place()).isEqualTo(Place.FIRST);

        PlayerFinishedMessage playerFinishedSecond = playerFinishedQueueFirst.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(playerFinishedSecond).isNotNull();
        assertThat(playerFinishedSecond.place()).isEqualTo(Place.SECOND);

        ContestFinishedMessage finishedMessage = finishedQueueFirst.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(finishedMessage).isNotNull();
        assertThat(finishedMessage.leaderboard()).hasSize(2);
        assertThat(finishedMessage.leaderboard().get(0).place()).isEqualTo(Place.FIRST);
        assertThat(finishedMessage.leaderboard().get(1).place()).isEqualTo(Place.SECOND);

        assertThat(contestRepo.findById(idContestCreated))
                .isPresent()
                .hasValueSatisfying(contest ->
                        assertThat(contest.getStatus()).isEqualTo(Status.FINISHED)
                );

        assertThat(resultItemRepo.findAllByContestIdOrderByPlaceAsc(idContestCreated)).hasSize(2);

        sessionFirst.disconnect();
        sessionSecond.disconnect();
    }

    @Test
    @DisplayName("Создание комнаты и присоединение второго игрока с получением уведомления")
    void createAndJoinRoom_ShouldBroadcastPlayerJoined_WhenSecondPlayerJoins() throws Exception {
        JoinRoomRsDto responseCreate = createRoom(tokenPlayerFirst, exerciseSaved.getId(), 2);
        idContestCreated = responseCreate.idContest();

        StompSession sessionFirst = connectToWebSocket(tokenPlayerFirst);
        String topicPlayerJoined = String.format("/topic/contest/%d/player-joined", idContestCreated);
        BlockingQueue<PlayerJoinedMessage> playerJoinedQueue = subscribeToTopic(sessionFirst, topicPlayerJoined, PlayerJoinedMessage.class);

        Thread.sleep(300);

        joinRoom(tokenPlayerSecond, idContestCreated);

        PlayerJoinedMessage playerJoinedMessage = playerJoinedQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(playerJoinedMessage).isNotNull();
        assertThat(playerJoinedMessage.userName()).isEqualTo(USERNAME_SECOND);
        assertThat(playerJoinedMessage.currentPlayers()).isEqualTo(2);
        assertThat(playerJoinedMessage.maxPlayers()).isEqualTo(2);

        sessionFirst.disconnect();
    }

    @Test
    @DisplayName("Готовность одного участника не запускает обратный отсчёт")
    void processReady_ShouldNotStartCountdown_WhenOnlyOneParticipantReady() throws Exception {
        JoinRoomRsDto responseCreate = createRoom(tokenPlayerFirst, exerciseSaved.getId(), 2);
        idContestCreated = responseCreate.idContest();

        joinRoom(tokenPlayerSecond, idContestCreated);

        StompSession sessionFirst = connectToWebSocket(tokenPlayerFirst);
        String topicCountdown = String.format("/topic/contest/%d/countdown", idContestCreated);
        BlockingQueue<CountdownMessage> countdownQueue = subscribeToTopic(sessionFirst, topicCountdown, CountdownMessage.class);

        Thread.sleep(300);

        String destinationReady = String.format("/app/contest/%d/ready", idContestCreated);
        sessionFirst.send(destinationReady, null);

        CountdownMessage countdownMessage = countdownQueue.poll(3, TimeUnit.SECONDS);
        assertThat(countdownMessage).isNull();

        assertThat(contestRepo.findById(idContestCreated))
                .isPresent()
                .hasValueSatisfying(contest ->
                        assertThat(contest.getStatus()).isEqualTo(Status.CREATED)
                );

        sessionFirst.disconnect();
    }

    @Test
    @DisplayName("Обратный отсчёт начинается когда все участники готовы")
    void processReady_ShouldStartCountdown_WhenAllParticipantsReady() throws Exception {
        JoinRoomRsDto responseCreate = createRoom(tokenPlayerFirst, exerciseSaved.getId(), 2);
        idContestCreated = responseCreate.idContest();

        joinRoom(tokenPlayerSecond, idContestCreated);

        StompSession sessionFirst = connectToWebSocket(tokenPlayerFirst);
        StompSession sessionSecond = connectToWebSocket(tokenPlayerSecond);

        String topicCountdown = String.format("/topic/contest/%d/countdown", idContestCreated);
        BlockingQueue<CountdownMessage> countdownQueue = subscribeToTopic(sessionFirst, topicCountdown, CountdownMessage.class);

        Thread.sleep(300);

        String destinationReady = String.format("/app/contest/%d/ready", idContestCreated);
        sessionFirst.send(destinationReady, null);
        sessionSecond.send(destinationReady, null);

        CountdownMessage countdownMessage = countdownQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(countdownMessage).isNotNull();
        assertThat(countdownMessage.seconds()).isLessThanOrEqualTo(5);

        sessionFirst.disconnect();
        sessionSecond.disconnect();
    }

    private String registerAndLogin(String username, String password) {
        RegistrationRequest registrationRequest = new RegistrationRequest(username, password, password);

        restTemplate.postForEntity(
                "/auth/register",
                registrationRequest,
                Void.class
        );

        AuthenticationRequest authRequest = new AuthenticationRequest(username, password);
        ResponseEntity<AuthenticationResponse> responseAuth = restTemplate.postForEntity(
                "/auth/login",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(responseAuth.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseAuth.getBody()).isNotNull();

        return responseAuth.getBody().accessToken();
    }

    private Exercise createExercise() {
        var user = userRepo.findUserByUsername(USERNAME_FIRST);
        return exerciseRepo.save(new Exercise("E2E Test Exercise", EXERCISE_TEXT_TEST, user));
    }

    private JoinRoomRsDto createRoom(String token, Long idExercise, int maxParticipants) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        CreateRoomRqDto requestBody = new CreateRoomRqDto(idExercise, maxParticipants);

        ResponseEntity<JoinRoomRsDto> response = restTemplate.exchange(
                "/multiplayer/room",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                JoinRoomRsDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private JoinRoomRsDto joinRoom(String token, Long idContest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<JoinRoomRsDto> response = restTemplate.exchange(
                "/multiplayer/room/" + idContest + "/join",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                JoinRoomRsDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
