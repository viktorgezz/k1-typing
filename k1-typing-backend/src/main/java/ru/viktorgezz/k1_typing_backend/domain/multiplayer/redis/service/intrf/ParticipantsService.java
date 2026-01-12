package ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf;

import java.util.Map;
import java.util.Set;

public interface ParticipantsService {

    void addParticipant(Long idContest, Long idUser, String username);

    void removeParticipant(Long idContest, Long idUser);

    Set<Long> getParticipantIds(Long idContest);

    Map<Long, String> getParticipantNames(Long idContest);

    int getParticipantsCount(Long idContest);

    boolean isRoomFull(Long idContest);

    boolean isParticipant(Long idContest, Long idUser);
}
