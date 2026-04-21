package ru.viktorgezz.statistics_result_module.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.statistics_result_module.statistics.dto.ExerciseRecordRsDto;
import ru.viktorgezz.statistics_result_module.statistics.dto.UserLeaderboardRsDto;
import ru.viktorgezz.statistics_result_module.statistics.dto.UserPersonalStatisticsRsDto;
import ru.viktorgezz.statistics_result_module.statistics.service.intrf.ExerciseRecordService;
import ru.viktorgezz.statistics_result_module.statistics.service.intrf.UserLeaderboardService;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final UserLeaderboardService userLeaderboardService;
    private final ExerciseRecordService exerciseRecordService;

    @GetMapping("/leaderboard")
    public List<UserLeaderboardRsDto> getTop10Users() {
        return userLeaderboardService.getTop10Users();
    }

    @GetMapping("/personal")
    public UserPersonalStatisticsRsDto getUserPersonalStatisticsRsDto() {
        return userLeaderboardService.getUserPersonalStatistics();
    }

    @GetMapping("/exercise")
    public List<ExerciseRecordRsDto> getAllExerciseStatistics() {
        return exerciseRecordService.getExerciseStatistics();
    }


}
