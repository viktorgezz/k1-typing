package ru.viktorgezz.coretyping.domain.exercises.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;

public interface ExerciseRepo extends CrudRepository<Exercise, Long> {
}
