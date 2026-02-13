package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf;

import java.util.Set;

public interface ReadyService {

    void markReady(Long idContest, Long idUser);

    void unmarkReady(Long idContest, Long idUser);

    boolean isReady(Long idContest, Long idUser);

    Set<Long> getReadyParticipantIds(Long idContest);

    int getReadyCount(Long idContest);

    void clearReady(Long idContest);
}
