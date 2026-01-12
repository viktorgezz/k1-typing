package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ContestFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.FinishMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.PlayerFinishedMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.ProgressUpdateMessage;

/**
 * Сервис WebSocket-взаимодействия для мультиплеерных соревнований.
 * <p>
 * Управляет жизненным циклом соревнования: обработка готовности участников,
 * отсчёт перед стартом, отслеживание прогресса и финиша игроков,
 * а также рассылка событий через WebSocket.
 */
public interface ContestWebSocketService {

    /**
     * Обновляет прогресс печати участника и рассылает текущий прогресс всем игрокам.
     */
    void processProgress(Long idContest, Long idUser, ProgressUpdateMessage message);

    /**
     * Обрабатывает завершение печати участником: сохраняет результат и уведомляет остальных.
     * Если все участники завершили — инициирует завершение соревнования.
     */
    void processFinish(Long idContest, Long idUser, FinishMessage message);

    /**
     * Транзакционная часть обработки финиша: регистрирует место и сохраняет результат в БД.
     * Вызывается через self-injection для корректной работы {@code @Transactional}.
     */
    PlayerFinishedMessage processFinishTransaction(Long idContest, Long idUser, FinishMessage message);

    /**
     * Отмечает участника как готового. При готовности всех — запускает обратный отсчёт.
     */
    void processReady(Long idContest, Long idUser);

    /**
     * Рассылает уведомление о присоединении нового игрока в комнату.
     */
    void broadcastPlayerJoined(Long idContest, Long idUser, String username);

    /**
     * Рассылает уведомление о выходе игрока из комнаты и снимает его готовность.
     */
    void broadcastPlayerLeft(Long idContest, Long idUser, String username);

    /**
     * Завершает соревнование: фиксирует статус FINISHED и рассылает лидерборд.
     */
    void finishContest(Long idContest);

    /**
     * Транзакционная часть завершения: обновляет статус и формирует лидерборд.
     * Вызывается через self-injection для корректной работы {@code @Transactional}.
     */
    ContestFinishedMessage finishContestTransaction(Long idContest);
}
