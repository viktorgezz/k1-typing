package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf;

/**
 * Сервис управления комнатой
 */
public interface RoomService {

    void createRoom(Long idContest, Long idExercise, int participantsMax);

    void deleteRoom(Long idContest);

    boolean roomExists(Long idContest);

    int getParticipantsMax(Long idContest);
}
