package ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq.ResulItemRqDto;

public interface ResultItemCommandService {

    RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto);

    ResultItem saveResultMultiplayer(MultiplayerResultItemDto dto);
}
