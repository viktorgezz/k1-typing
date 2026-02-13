package ru.viktorgezz.coretyping.domain.result_item.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ru.viktorgezz.coretyping.domain.result_item.ResultItem;

public interface ResultItemRepo extends CrudRepository<ResultItem, Long> {

    @Query("""
            SELECT r FROM ResultItem r
            JOIN FETCH r.user
            WHERE r.contest.id = :idContest
            ORDER BY r.place ASC
            """)
    List<ResultItem> findAllByContestIdOrderByPlaceAsc(@Param("idContest") Long idContest);
}
