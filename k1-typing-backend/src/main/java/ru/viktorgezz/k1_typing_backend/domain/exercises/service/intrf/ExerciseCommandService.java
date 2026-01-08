package ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rq.CreationExerciseRqDto;
import ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rq.UpdatedExerciseRqDto;

import java.util.List;

public interface ExerciseCommandService {

    void create(CreationExerciseRqDto creationExerciseRqDto);

    void update(UpdatedExerciseRqDto updatedExerciseRqDto);

    Exercise delete(Long id);

    void deleteMany(List<Long> ids);

}
