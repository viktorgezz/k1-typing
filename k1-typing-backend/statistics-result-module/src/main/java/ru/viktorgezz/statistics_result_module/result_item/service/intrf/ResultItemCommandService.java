package ru.viktorgezz.statistics_result_module.result_item.service.intrf;


import ru.viktorgezz.statistics_result_module.result_item.ResultItem;
import ru.viktorgezz.statistics_result_module.result_item.dto.rq.MultiplayerResultItemDto;
import ru.viktorgezz.statistics_result_module.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.statistics_result_module.result_item.dto.rs.RecordedResulItemRsDto;

public interface ResultItemCommandService {

    RecordedResulItemRsDto saveResultSingleContest(ResulItemRqDto dto);

    ResultItem saveResultMultiplayer(MultiplayerResultItemDto dto);
}
