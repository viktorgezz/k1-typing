package ru.viktorgezz.statistics_result_module.result_item.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import ru.viktorgezz.statistics_result_module.result_item.ResultItem;

public interface ResultItemPagingAndSortingRepo extends PagingAndSortingRepository<ResultItem, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<ResultItem> findAll(@NonNull Pageable pageable);

    Page<ResultItem> findByIdUser(Long userId, Pageable pageable);
}
