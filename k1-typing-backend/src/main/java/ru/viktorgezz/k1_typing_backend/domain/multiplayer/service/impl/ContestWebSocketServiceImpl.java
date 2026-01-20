package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.*;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.*;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf.ContestWebSocketService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemCommandService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemQueryService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.util.WebsocketTopicStorage.*;

@Slf4j
@Service
public class ContestWebSocketServiceImpl implements ContestWebSocketService {

    private static final int COUNTDOWN_SECONDS = 5;
    private static final int MIN_COUNT_PARTICIPANTS = 2;

    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;

    private final ProgressService progressService;
    private final ParticipantsService participantsService;
    private final ReadyService readyService;
    private final RoomService roomService;
    private final FinishService finishService;

    private final ContestQueryService contestQueryService;
    private final ContestCommandService contestCommandService;
    private final ResultItemCommandService resultItemCommandService;
    private final ResultItemQueryService resultItemQueryService;

    private final ContestWebSocketService self;

    private final ScheduledExecutorService executorScheduled = Executors.newScheduledThreadPool(2);

    @Autowired
    public ContestWebSocketServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            TransactionTemplate transactionTemplate,
            ProgressService progressService,
            ParticipantsService participantsService,
            ReadyService readyService,
            RoomService roomService,
            FinishService finishService,
            ContestQueryService contestQueryService,
            ContestCommandService contestCommandService,
            ResultItemCommandService resultItemCommandService,
            ResultItemQueryService resultItemQueryService,
            @Lazy ContestWebSocketService self
    ) {
        this.messagingTemplate = messagingTemplate;
        this.transactionTemplate = transactionTemplate;
        this.progressService = progressService;
        this.participantsService = participantsService;
        this.readyService = readyService;
        this.roomService = roomService;
        this.finishService = finishService;
        this.contestQueryService = contestQueryService;
        this.contestCommandService = contestCommandService;
        this.resultItemCommandService = resultItemCommandService;
        this.resultItemQueryService = resultItemQueryService;
        this.self = self;
    }

    // Сохраняет прогресс в Redis и рассылает обновлённый прогресс всех участников
    @Override
    public void processProgress(Long idContest, Long idUser, ProgressUpdateMessage message) {
        progressService.updateProgress(idContest, idUser, message.progress(), message.speed(), message.accuracy());
        messagingTemplate.convertAndSend(
                String.format(TOPIC_PROGRESS, idContest),
                new AllProgressMessage(progressService.getProgressAll(idContest))
        );
    }

    // Вызывает транзакционную логику финиша, рассылает результат и проверяет завершение соревнования
    @Override
    public void processFinish(Long idContest, Long idUser, FinishMessage message) {
        PlayerFinishedMessage messageFinished = self.processFinishTransaction(idContest, idUser, message);

        messagingTemplate.convertAndSend(
                String.format(TOPIC_PLAYER_FINISHED, idContest),
                messageFinished
        );

        if (finishService.isContestComplete(idContest)) {
            self.finishContest(idContest);
        }
    }

    // Регистрирует место финиширования, сохраняет результат в PostgreSQL, формирует сообщение
    @Override
    @Transactional
    public PlayerFinishedMessage processFinishTransaction(Long idContest, Long idUser, FinishMessage message) {
        final Place placeObtained = finishService.registerFinish(idContest, idUser);

        Map<Long, String> participantNames = participantsService.getParticipantNames(idContest);
        final String username = participantNames.getOrDefault(idUser, "Unknown");

        resultItemCommandService.saveResultMultiplayer(
                new MultiplayerResultItemDto(
                        idContest,
                        idUser,
                        message.durationSeconds(),
                        message.speed(),
                        message.accuracy(),
                        placeObtained
                )
        );

        return new PlayerFinishedMessage(
                idUser,
                username,
                placeObtained,
                message.speed(),
                message.durationSeconds(),
                message.accuracy()
        );
    }

    // Отмечает готовность в Redis; при готовности всех — меняет статус на WAITING и запускает отсчёт
    @Override
    @Transactional
    public void processReady(Long idContest, Long idUser) {
        readyService.markReady(idContest, idUser);

        final int countParticipants = participantsService.getParticipantsCount(idContest);
        final int countParticipantsMax = roomService.getParticipantsMax(idContest);
        final int countReady = readyService.getReadyCount(idContest);

        log.debug("Contest {}: {}/{} participants ready. Max: {}", idContest, countReady, countParticipants, countParticipantsMax);

        // Отправляем broadcast о готовности игрока
        messagingTemplate.convertAndSend(
                String.format(TOPIC_PLAYER_READY, idContest),
                new PlayerReadyMessage(idUser, countReady, countParticipants)
        );

        if (countReady >= ((countParticipantsMax + 1) / 2) && countParticipants >= MIN_COUNT_PARTICIPANTS) {
            Contest contest = contestQueryService.getOne(idContest);
            contest.setStatus(Status.WAITING);
            contestCommandService.save(contest);
            startCountdown(idContest);
        }
    }

    // Формирует сообщение о присоединении игрока с текущим и максимальным числом участников
    @Override
    public void broadcastPlayerJoined(Long idContest, Long idUser, String username) {
        int countCurrent = participantsService.getParticipantsCount(idContest);
        int countMax = roomService.getParticipantsMax(idContest);

        PlayerJoinedMessage messageJoined = new PlayerJoinedMessage(
                idUser,
                username,
                countCurrent,
                countMax
        );

        messagingTemplate.convertAndSend(
                String.format(TOPIC_PLAYER_JOINED, idContest),
                messageJoined
        );
    }

    // Рассылает уведомление о выходе игрока и снимает его готовность в Redis
    @Override
    public void broadcastPlayerLeft(Long idContest, Long idUser, String username) {
        final int countCurrent = participantsService.getParticipantsCount(idContest);

        PlayerLeftRoomMessage messageLeft = new PlayerLeftRoomMessage(
                idUser,
                username,
                countCurrent
        );

        messagingTemplate.convertAndSend(
                String.format(TOPIC_PLAYER_LEFT, idContest),
                messageLeft
        );

        readyService.unmarkReady(idContest, idUser);
    }

    // Вызывает транзакционную логику завершения и рассылает лидерборд участникам
    @Override
    public void finishContest(Long idContest) {
        ContestFinishedMessage messageFinished = self.finishContestTransaction(idContest);

        messagingTemplate.convertAndSend(
                String.format(TOPIC_FINISHED, idContest),
                messageFinished
        );

        log.info("Contest {} finished", idContest);
    }

    // Устанавливает статус FINISHED и формирует лидерборд из результатов PostgreSQL
    @Override
    @Transactional
    public ContestFinishedMessage finishContestTransaction(Long idContest) {
        Contest contest = contestQueryService.getOne(idContest);
        contest.setStatus(Status.FINISHED);
        contestCommandService.save(contest);

        return new ContestFinishedMessage(buildLeaderboard(idContest));
    }

    // Собирает лидерборд: маппит результаты из БД с именами участников из Redis
    private List<ContestFinishedMessage.LeaderboardEntry> buildLeaderboard(Long idContest) {
        Map<Long, String> participantNames = participantsService.getParticipantNames(idContest);

        return resultItemQueryService.findAllByContest(idContest)
                .stream()
                .map(result -> new ContestFinishedMessage.LeaderboardEntry(
                        result.getUser().getId(),
                        participantNames.getOrDefault(result.getUser().getId(), "Unknown"),
                        result.getPlace(),
                        result.getDurationSeconds(),
                        result.getSpeed(),
                        result.getAccuracy()
                ))
                .toList();
    }

    // Планирует отправку countdown-сообщений каждую секунду; при 0 — стартует соревнование
    private void startCountdown(Long idContest) {
        IntStream.rangeClosed(0, COUNTDOWN_SECONDS).forEach(delayInSeconds -> {
            int secondsRemaining = COUNTDOWN_SECONDS - delayInSeconds;
            executorScheduled.schedule(() -> {
                try {
                    messagingTemplate.convertAndSend(
                            String.format(TOPIC_COUNTDOWN, idContest),
                            new CountdownMessage(secondsRemaining)
                    );

                    if (secondsRemaining == 0) {
                        ContestStartMessage messageStart = transactionTemplate.execute(status ->
                                buildStartMessage(idContest)
                        );

                        if (messageStart == null) {
                            log.error("Failed to start contest {} - transaction returned null", idContest);
                            return;
                        }

                        messagingTemplate.convertAndSend(
                                String.format(TOPIC_START, idContest),
                                messageStart
                        );

                        readyService.clearReady(idContest);

                        log.info("Contest {} started", idContest);
                    }
                } catch (Exception e) {
                    log.error("Error sending countdown for contest {}: {}", idContest, e.getMessage());
                }
            }, delayInSeconds, TimeUnit.SECONDS);
        });
    }

    // Переводит соревнование в статус PROGRESS и формирует сообщение с текстом и временем старта
    private ContestStartMessage buildStartMessage(Long idContest) {
        Contest contest = contestQueryService.getOneWithExercise(idContest);
        contest.setStatus(Status.PROGRESS);
        contestCommandService.save(contest);

        final String textExercise = contest.getExercise().getText();
        final long timestampStart = System.currentTimeMillis();

        return new ContestStartMessage(textExercise, timestampStart);
    }
}
