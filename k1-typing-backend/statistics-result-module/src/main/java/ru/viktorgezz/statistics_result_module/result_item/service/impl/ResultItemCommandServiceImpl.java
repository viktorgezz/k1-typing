package ru.viktorgezz.statistics_result_module.result_item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.statistics_result_module.result_item.Place;
import ru.viktorgezz.statistics_result_module.result_item.ResultItem;
import ru.viktorgezz.statistics_result_module.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.statistics_result_module.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.statistics_result_module.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.statistics_result_module.result_item.repo.ResultItemRepo;
import ru.viktorgezz.statistics_result_module.result_item.service.intrf.ResultItemCommandService;

import static ru.viktorgezz.statistics_result_module.util.CurrentUserUtils.getIdUserNullable;


@Service
@RequiredArgsConstructor
public class ResultItemCommandServiceImpl implements ResultItemCommandService {

    private final ResultItemRepo resultItemRepo;

    @Override
    @Transactional
    public RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto) {

        final Long idUser = getIdUserNullable();

        ResultItem resultItem = resultItemRepo.save(
                new ResultItem(
                        dto.durationSeconds(),
                        dto.speed(),
                        dto.accuracy(),
                        Place.WITHOUT_PLACE,
                        null,
                        idUser
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
        return resultItemRepo.save(
                new ResultItem(
                        dto.durationSeconds(),
                        dto.speed(),
                        dto.accuracy(),
                        dto.place(),
                        dto.idContest(),
                        dto.idUser()
                )
        );
    }
}
