package ru.viktorgezz.statistics_result_module.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.statistics_result_module.statistics.dto.ExerciseRecordRsDto;
import ru.viktorgezz.statistics_result_module.statistics.repo.ExerciseRecordRepo;
import ru.viktorgezz.statistics_result_module.statistics.service.intrf.ExerciseRecordService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseRecordServiceImpl implements ExerciseRecordService {

    private final ExerciseRecordRepo exerciseRecordRepo;

    @Override
    public List<ExerciseRecordRsDto> getExerciseStatistics() {
        return exerciseRecordRepo
                .findAll()
                .stream()
                .map(statistics -> new ExerciseRecordRsDto(
                        statistics.getTitle(),
                        statistics.getUsername(),
                        statistics.getMaxSpeed(),
                        statistics.getMinDuration()
                ))
                .toList();
    }
}
