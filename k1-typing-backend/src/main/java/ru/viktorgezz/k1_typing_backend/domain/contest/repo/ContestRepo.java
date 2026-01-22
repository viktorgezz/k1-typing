package ru.viktorgezz.k1_typing_backend.domain.contest.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ContestRepo extends CrudRepository<Contest, Long> {

    @EntityGraph(attributePaths = {"exercise"})
    @Query("SELECT c FROM Contest c WHERE c.id = :id")
    Optional<Contest> findContestWithExercise(@NonNull Long id);

    @Query("""
            SELECT COUNT(c) > 0 FROM Contest c
            WHERE c.id = :id AND c.createdAt <= :createdBefore
            """)
    boolean existsByIdAndCreatedAtBefore(@NonNull Long id, @NonNull LocalDateTime createdBefore);

    @Modifying
    @Query("""
            DELETE FROM Contest c
            WHERE c.createdAt <= :createdBefore
            AND c.status = :status
            """)
    long deleteOldContestsByStatus(@NonNull LocalDateTime createdBefore, @NonNull Status status);

}
