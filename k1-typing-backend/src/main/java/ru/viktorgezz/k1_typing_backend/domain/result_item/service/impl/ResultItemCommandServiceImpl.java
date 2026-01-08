package ru.viktorgezz.k1_typing_backend.domain.result_item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemCommandService;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

import static ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class ResultItemCommandServiceImpl implements ResultItemCommandService {

    private final ContestQueryService contestQueryService;
    private final ResultItemRepo resultItemRepo;

    @Override
    @Transactional
    public RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto) {
        Contest contest = contestQueryService.getOne(dto.idContest());
        contest.setStatus(Status.FINISHED);
        User userCurrent;
        try {
            userCurrent = getCurrentUser();
        } catch (Exception e) {
            userCurrent = null;
        }

        ResultItem resultItem = resultItemRepo.save(
                new ResultItem(
                        dto.durationSeconds(),
                        dto.speed(),
                        dto.accuracy(),
                        Place.WITHOUT_PLACE,
                        contest,
                        userCurrent
                )
        );

        return new RecordedResulItemRsDto(
                resultItem.getDurationSeconds(),
                resultItem.getSpeed(),
                resultItem.getAccuracy(),
                resultItem.getPlace()
        );
    }
}
