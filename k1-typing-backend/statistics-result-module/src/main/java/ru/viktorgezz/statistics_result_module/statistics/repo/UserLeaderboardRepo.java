package ru.viktorgezz.statistics_result_module.statistics.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktorgezz.statistics_result_module.statistics.model.UserLeaderboard;

import java.util.Optional;

public interface UserLeaderboardRepo extends JpaRepository<UserLeaderboard, Long> {

    Optional<UserLeaderboard> findByIdUser(Long idUser);
}
