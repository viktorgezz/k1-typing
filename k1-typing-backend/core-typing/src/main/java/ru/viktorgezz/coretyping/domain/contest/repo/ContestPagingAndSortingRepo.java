package ru.viktorgezz.coretyping.domain.contest.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;

import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;

public interface ContestPagingAndSortingRepo extends PagingAndSortingRepository<Contest, Long> {

    @EntityGraph(attributePaths = {"exercise"})
    Page<Contest> findByAmountGreaterThanAndStatus(Integer amount, Status status, Pageable pageable);
}
