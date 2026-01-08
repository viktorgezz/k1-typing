package ru.viktorgezz.k1_typing_backend.domain.result_item.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;

public interface ResultItemRepo extends CrudRepository<ResultItem, Long> {
}
