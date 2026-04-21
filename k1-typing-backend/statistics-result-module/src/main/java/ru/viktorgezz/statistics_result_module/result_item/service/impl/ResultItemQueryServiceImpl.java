package ru.viktorgezz.statistics_result_module.result_item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.statistics_result_module.result_item.ResultItem;
import ru.viktorgezz.statistics_result_module.result_item.dto.rs.ResultListItemRsDto;
import ru.viktorgezz.statistics_result_module.result_item.repo.ResultItemPagingAndSortingRepo;
import ru.viktorgezz.statistics_result_module.result_item.repo.ResultItemRepo;
import ru.viktorgezz.statistics_result_module.result_item.service.intrf.ResultItemQueryService;
import ru.viktorgezz.statistics_result_module.util.CurrentUserUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultItemQueryServiceImpl implements ResultItemQueryService {

    private final ResultItemPagingAndSortingRepo resultItemPagingAndSortingRepo;
    private final ResultItemRepo resultItemRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<ResultListItemRsDto> findAllByUser(Pageable pageable) {
        Long idUserCurrent = CurrentUserUtils.getIdUser();
        return resultItemPagingAndSortingRepo.findByIdUser(idUserCurrent, pageable)
                .map(
                        resultItem -> new ResultListItemRsDto(
                                resultItem.getDurationSeconds(),
                                resultItem.getSpeed(),
                                resultItem.getAccuracy(),
                                resultItem.getPlace()
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultItem> findAllByContest(Long idContest) {
        return resultItemRepo.findAllByContestIdOrderByPlaceAsc(idContest);
    }
}
