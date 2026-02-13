package ru.viktorgezz.coretyping.domain.exercises.service.intrf;

import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.CreationExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.UpdatedExerciseRqDto;

import java.util.List;

public interface ExerciseCommandService {

    void create(CreationExerciseRqDto creationExerciseRqDto);

    void update(UpdatedExerciseRqDto updatedExerciseRqDto);

    Exercise delete(Long id);

    void deleteMany(List<Long> ids);

}
