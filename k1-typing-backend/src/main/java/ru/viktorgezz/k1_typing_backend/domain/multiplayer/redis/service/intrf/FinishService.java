package ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;

public interface FinishService {

    Place registerFinish(Long idContest, Long idUser);

    int getFinishersCount(Long idContest);

    boolean isContestComplete(Long idContest);
}
