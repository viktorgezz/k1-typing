package ru.viktorgezz.coretyping.domain.contest.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;

public interface ContestQueryService {

    Contest getOne(Long id);

    Contest getOneWithExercise(Long id);

    boolean hasOldContest(Long id);

    Page<Contest> findByAmountGreaterThanAndStatus(Integer amount, Status status, Pageable pageable);
}
