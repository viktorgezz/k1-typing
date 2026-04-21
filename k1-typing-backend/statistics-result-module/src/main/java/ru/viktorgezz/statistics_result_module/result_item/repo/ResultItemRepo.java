package ru.viktorgezz.statistics_result_module.result_item.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.statistics_result_module.result_item.ResultItem;

import java.util.List;

public interface ResultItemRepo extends CrudRepository<ru.viktorgezz.statistics_result_module.result_item.ResultItem, Long> {

    @Query("""
            SELECT r FROM ResultItem r
            WHERE r.idContest= :idContest
            ORDER BY r.place ASC
            """)
    List<ResultItem> findAllByContestIdOrderByPlaceAsc(@Param("idContest") Long idContest);
}
