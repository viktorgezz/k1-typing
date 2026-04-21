package ru.viktorgezz.statistics_result_module.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.statistics_result_module.statistics.dto.UserLeaderboardRsDto;
import ru.viktorgezz.statistics_result_module.statistics.dto.UserPersonalStatisticsRsDto;
import ru.viktorgezz.statistics_result_module.statistics.model.UserLeaderboard;
import ru.viktorgezz.statistics_result_module.statistics.repo.UserLeaderboardRepo;
import ru.viktorgezz.statistics_result_module.statistics.service.intrf.UserLeaderboardService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static ru.viktorgezz.statistics_result_module.util.CurrentUserUtils.getIdUser;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLeaderboardServiceImpl implements UserLeaderboardService {

    private final UserLeaderboardRepo userLeaderboardRepo;

    @Override
    public List<UserLeaderboardRsDto> getTop10Users() {
        Pageable limitTen = PageRequest.of(0, 10, Sort.by("rankPlace").ascending());
        return userLeaderboardRepo.findAll(limitTen)
                .stream()
                .map(statistics -> new UserLeaderboardRsDto(
                                statistics.getRankPlace(),
                                statistics.getUsername(),
                                statistics.getAverageSpeed(),
                                statistics.getCountContest(),
                                statistics.getFirstPlacesCount()
                        )
                ).toList();
    }

    @Override
    public UserPersonalStatisticsRsDto getUserPersonalStatistics() {
        Long idUser = getIdUser();
        Optional<UserLeaderboard> userStatisticsOpt = userLeaderboardRepo.findByIdUser(idUser);

        if (userStatisticsOpt.isEmpty()) {
            return new UserPersonalStatisticsRsDto(0L, 0L, BigDecimal.ZERO, 0L);
        }
        UserLeaderboard userStatistics = userStatisticsOpt.orElseThrow();

        return new UserPersonalStatisticsRsDto(
                userStatistics.getRankPlace(),
                userStatistics.getCountContest(),
                userStatistics.getAverageSpeed(),
                userStatistics.getFirstPlacesCount()
        );
    }
}
