package ru.viktorgezz.k1_typing_backend.domain.contest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.k1_typing_backend.domain.contest.dto.CreationContestRqDto;
import ru.viktorgezz.k1_typing_backend.domain.contest.dto.CreationContestRsDto;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;

import java.util.List;

@RestController
@RequestMapping("/contest")
@RequiredArgsConstructor
public class ContestController {

    private final ContestCommandService contestCommandService;

    @PostMapping("/single")
    public CreationContestRsDto createSingleContest(
            @Valid @RequestBody CreationContestRqDto creationContestRqDto
    ) {
        return new CreationContestRsDto(
                contestCommandService.createSingle(creationContestRqDto).getId()
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contestCommandService.delete(id);
    }


    @DeleteMapping("/many")
    public void deleteMany(@RequestParam List<Long> ids) {
        contestCommandService.deleteMany(ids);
    }
}
