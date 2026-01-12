package ru.viktorgezz.k1_typing_backend.domain.contest.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;

public interface ContestRepo extends CrudRepository<Contest, Long> {

    @EntityGraph(attributePaths = {"exercise"})
    @Query("SELECT c FROM Contest c WHERE c.id = :id")
    Optional<Contest> findContestWithExercise(Long id);
}
