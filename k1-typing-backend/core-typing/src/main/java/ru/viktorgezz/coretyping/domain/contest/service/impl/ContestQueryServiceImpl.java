package ru.viktorgezz.coretyping.domain.contest.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;
import ru.viktorgezz.coretyping.domain.contest.repo.ContestPagingAndSortingRepo;
import ru.viktorgezz.coretyping.domain.contest.repo.ContestRepo;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContestQueryServiceImpl implements ContestQueryService {

    private final ContestRepo contestRepo;
    private final ContestPagingAndSortingRepo contestPagingAndSortingRepo;

    @Override
    @Transactional(readOnly = true)
    public Contest getOne(Long id) {
        return contestRepo.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.CONTEST_NOT_FOUND, String.valueOf(id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Contest getOneWithExercise(Long id) {
        return contestRepo.findContestWithExercise(id).orElseThrow(
                () -> new BusinessException(ErrorCode.CONTEST_NOT_FOUND, String.valueOf(id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOldContest(Long id) {
        return contestRepo.existsByIdAndCreatedAtBefore(id, LocalDateTime.now()
                .minusMinutes(RedisKeyGenerator.TTL_ROOM.toMinutes()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contest> findByAmountGreaterThanAndStatus(Integer amount, Status status, Pageable pageable) {
        return contestPagingAndSortingRepo.findByAmountGreaterThanAndStatus(amount, status, pageable);
    }

}

