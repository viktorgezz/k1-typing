package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf;

import ru.viktorgezz.coretyping.domain.result_item.Place;

public interface FinishService {

    Place registerFinish(Long idContest, Long idUser);

    int getFinishersCount(Long idContest);

    boolean isContestComplete(Long idContest);
}
