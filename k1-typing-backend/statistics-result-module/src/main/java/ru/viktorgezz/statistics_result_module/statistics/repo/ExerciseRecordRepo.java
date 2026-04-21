package ru.viktorgezz.statistics_result_module.statistics.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktorgezz.statistics_result_module.statistics.model.ExerciseRecord;

public interface ExerciseRecordRepo extends JpaRepository<ExerciseRecord, Long> {

}
