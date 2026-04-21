package ru.viktorgezz.statistics_result_module.statistics.service.intrf;

import ru.viktorgezz.statistics_result_module.statistics.dto.UserLeaderboardRsDto;
import ru.viktorgezz.statistics_result_module.statistics.dto.UserPersonalStatisticsRsDto;

import java.util.List;

public interface UserLeaderboardService {

    List<UserLeaderboardRsDto> getTop10Users();

    UserPersonalStatisticsRsDto getUserPersonalStatistics();
}
