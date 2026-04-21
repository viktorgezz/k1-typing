package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf;

import ru.viktorgezz.statistics_result_module.result_item.Place;

public interface FinishService {

    Place registerFinish(Long idContest, Long idUser);

    int getFinishersCount(Long idContest);

    boolean isContestComplete(Long idContest);
}
