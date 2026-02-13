package ru.viktorgezz.coretyping.domain.exercises;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.CreationExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.UpdatedExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rs.ExerciseGetByIdRsDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rs.ExerciseListItemRsDto;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseCommandService;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseQueryService;

import java.util.List;

@RestController
@RequestMapping("/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseCommandService exerciseCommandService;
    private final ExerciseQueryService exerciseQueryService;

    @GetMapping
    public PagedModel<ExerciseListItemRsDto> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<ExerciseListItemRsDto> exercises = exerciseQueryService.findAll(pageable);
        return new PagedModel<>(exercises);
    }

    @GetMapping("/{id}")
    public ExerciseGetByIdRsDto getOne(@PathVariable Long id) {
        return new ExerciseGetByIdRsDto(exerciseQueryService.getOne(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(
        @Valid @RequestBody CreationExerciseRqDto dto
    ) {
        exerciseCommandService.create(dto);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void update(
        @Valid @RequestBody UpdatedExerciseRqDto dto
    ) {
        exerciseCommandService.update(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Exercise delete(@PathVariable Long id) {
        return exerciseCommandService.delete(id);
    }


    @DeleteMapping("/many")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMany(@RequestParam List<Long> ids) {
        exerciseCommandService.deleteMany(ids);
    }
}
