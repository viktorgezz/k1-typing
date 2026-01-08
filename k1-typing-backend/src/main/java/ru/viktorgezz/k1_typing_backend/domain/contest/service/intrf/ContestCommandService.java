package ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.dto.CreationContestRqDto;

import java.util.List;

public interface ContestCommandService {

    Contest createSingle(CreationContestRqDto creationContestRqDto);

    Contest delete(Long id);

    void deleteMany(List<Long> ids);
}