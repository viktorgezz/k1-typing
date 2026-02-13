package ru.viktorgezz.coretyping.domain.contest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestCommandService;

import java.util.List;

@RestController
@RequestMapping("/contest")
@RequiredArgsConstructor
public class ContestController {

    private final ContestCommandService contestCommandService;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contestCommandService.delete(id);
    }


    @DeleteMapping("/many")
    public void deleteMany(@RequestParam List<Long> ids) {
        contestCommandService.deleteMany(ids);
    }
}
