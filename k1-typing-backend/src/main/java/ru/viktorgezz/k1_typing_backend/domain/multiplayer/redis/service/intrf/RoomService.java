package ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf;

/**
 * Сервис управления комнатой
 */
public interface RoomService {

    void createRoom(Long idContest, Long idExercise, int participantsMax);

    void deleteRoom(Long idContest);

    boolean roomExists(Long idContest);

    int getParticipantsMax(Long idContest);
}
