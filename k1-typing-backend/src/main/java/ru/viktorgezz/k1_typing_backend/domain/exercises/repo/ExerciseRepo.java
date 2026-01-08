package ru.viktorgezz.k1_typing_backend.domain.exercises.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;

public interface ExerciseRepo extends CrudRepository<Exercise, Long> {
}
