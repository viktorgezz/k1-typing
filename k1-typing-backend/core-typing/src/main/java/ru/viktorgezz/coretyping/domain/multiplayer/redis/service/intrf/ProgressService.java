package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf;

import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.AllProgressMessage.UserProgressData;

import java.math.BigDecimal;
import java.util.Map;

public interface ProgressService {

    void updateProgress(Long idContest, Long idUser, int progressPercent, int speed, BigDecimal accuracy);

    Map<Long, UserProgressData> getProgressAll(Long idContest);

    UserProgressData getProgressByUser(Long idContest, Long idUser);
}
