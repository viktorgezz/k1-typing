package ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.ResultListItemRsDto;

import java.util.List;

public interface ResultItemQueryService {

    Page<ResultListItemRsDto> findAllByUser(Pageable pageable);

    List<ResultItem> findAllByContest(Long idContest);
}
