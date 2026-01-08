package ru.viktorgezz.k1_typing_backend.domain.contest.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ContestQueryServiceImpl implements ContestQueryService {

    private final ContestRepo contestRepo;

    public Contest getOne(Long id) {
        return contestRepo.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.CONTEST_NOT_FOUND, String.valueOf(id))
        );
    }
}

