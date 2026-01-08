package ru.viktorgezz.k1_typing_backend.domain.result_item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.ResultListItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemPagingAndSortingRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemQueryService;
import ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils;

@Service
@RequiredArgsConstructor
public class ResultItemQueryServiceImpl implements ResultItemQueryService {

    private final ResultItemPagingAndSortingRepo resultItemPagingAndSortingRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<ResultListItemRsDto> findAllByUser(Pageable pageable) {
        Long idUserCurrent = CurrentUserUtils.getCurrentUser().getId();
        return resultItemPagingAndSortingRepo.findByUserId(idUserCurrent, pageable)
                .map(
                        resultItem -> new ResultListItemRsDto(
                                resultItem.getDurationSeconds(),
                                resultItem.getSpeed(),
                                resultItem.getAccuracy(),
                                resultItem.getPlace()
                        )
                );
    }
}
