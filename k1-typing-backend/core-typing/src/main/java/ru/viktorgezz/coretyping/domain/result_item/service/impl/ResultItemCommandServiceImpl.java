package ru.viktorgezz.coretyping.domain.result_item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;
import ru.viktorgezz.coretyping.domain.contest.dto.rq.CreationContestRqDto;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.coretyping.domain.result_item.Place;
import ru.viktorgezz.coretyping.domain.result_item.ResultItem;
import ru.viktorgezz.coretyping.domain.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.coretyping.domain.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.coretyping.domain.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.coretyping.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.coretyping.domain.result_item.service.intrf.ResultItemCommandService;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserQueryService;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class ResultItemCommandServiceImpl implements ResultItemCommandService {

    private final ContestCommandService contestCommandService;
    private final ContestQueryService contestQueryService;
    private final UserQueryService userQueryService;
    private final ResultItemRepo resultItemRepo;

    @Override
    @Transactional
    public RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto) {

        Contest contest = contestCommandService.createSingle(
                new CreationContestRqDto(dto.idExercises(), Status.FINISHED)
        );

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

    @Override
    @Transactional
    public ResultItem saveResultMultiplayer(MultiplayerResultItemDto dto) {
        Contest contest = contestQueryService.getOne(dto.idContest());
        User user = userQueryService.getOne(dto.idUser());

        return resultItemRepo.save(
                new ResultItem(
                        dto.durationSeconds(),
                        dto.speed(),
                        dto.accuracy(),
                        dto.place(),
                        contest,
                        user
                )
        );
    }
}
