package ru.viktorgezz.k1_typing_backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCleanupServiceImpl implements CleanupService {

    private final ContestCommandService contestCommandService;

    @Override
    @Transactional
    @Scheduled(cron = "0 30 3 * * *")
    @Scheduled(cron = "0 0 13 * * *")
    public long purgeData() {
        return purgeOldContestWithStatusCreated();
    }

    private long purgeOldContestWithStatusCreated() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long countDeleted = contestCommandService.deleteOldContestsByStatus(now
                    .minusMinutes(RedisKeyGenerator.TTL_ROOM.toMinutes()), Status.CREATED);
            log.debug("Purge {} old contest at {}", countDeleted, now);
            return countDeleted;
        } catch (Exception e) {
            log.error("Error purging old contest whit status CREATED {}", e.getMessage(), e);
            return 0;
        }
    }
}
