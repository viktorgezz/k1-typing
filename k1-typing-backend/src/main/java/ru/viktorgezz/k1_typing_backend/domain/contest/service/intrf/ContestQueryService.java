package ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;

public interface ContestQueryService {

    Contest getOne(Long id);
}
