package ru.viktorgezz.coretyping.domain.contest.service.intrf;

import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;
import ru.viktorgezz.coretyping.domain.contest.dto.rq.CreationContestRqDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ContestCommandService {

    Contest createSingle(CreationContestRqDto creationContestRqDto);

    Contest save(Contest contest);

    Contest delete(Long id);

    void deletePropagationRequiresNew(Long id);

    long deleteOldContestsByStatus(LocalDateTime createdBefore, Status status);

    void deleteMany(List<Long> ids);
}