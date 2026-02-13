package ru.viktorgezz.coretyping.domain.result_item.service.intrf;

import ru.viktorgezz.coretyping.domain.result_item.ResultItem;
import ru.viktorgezz.coretyping.domain.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.coretyping.domain.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.coretyping.domain.result_item.dto.rq.ResulItemRqDto;

public interface ResultItemCommandService {

    RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto);

    ResultItem saveResultMultiplayer(MultiplayerResultItemDto dto);
}
