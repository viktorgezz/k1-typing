package ru.viktorgezz.statistics_result_module.statistics.service.intrf;

import ru.viktorgezz.statistics_result_module.statistics.dto.ExerciseRecordRsDto;

import java.util.List;

public interface ExerciseRecordService {

    List<ExerciseRecordRsDto> getExerciseStatistics();
}
