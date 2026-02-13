package ru.viktorgezz.coretyping.domain.exercises.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.dto.rs.ExerciseListItemRsDto;

public interface ExerciseQueryService {

    Page<ExerciseListItemRsDto> findAll(Pageable pageable);

    Exercise getOne(Long id);
}
