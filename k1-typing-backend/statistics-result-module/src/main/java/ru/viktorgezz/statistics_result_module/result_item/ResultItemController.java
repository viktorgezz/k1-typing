package ru.viktorgezz.statistics_result_module.result_item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.statistics_result_module.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.statistics_result_module.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.statistics_result_module.result_item.service.intrf.ResultItemCommandService;

@RestController
@RequestMapping("/result_item")
@RequiredArgsConstructor
public class ResultItemController {

    private final ResultItemCommandService resultItemCommandService;

    @PostMapping("/single-contest")
    public RecordedResulItemRsDto recordSingleContestResult(
            @Valid @RequestBody ResulItemRqDto resulItemRqDto
    ) {
        return resultItemCommandService.saveResultSingleContest(resulItemRqDto);
    }
}
