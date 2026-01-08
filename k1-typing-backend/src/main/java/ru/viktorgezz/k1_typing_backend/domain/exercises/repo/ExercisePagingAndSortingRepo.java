package ru.viktorgezz.k1_typing_backend.domain.exercises.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;

public interface ExercisePagingAndSortingRepo extends PagingAndSortingRepository<Exercise, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<Exercise> findAll(@NonNull Pageable pageable);
}
