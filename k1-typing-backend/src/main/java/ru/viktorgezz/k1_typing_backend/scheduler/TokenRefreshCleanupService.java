package ru.viktorgezz.k1_typing_backend.scheduler;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.viktorgezz.k1_typing_backend.security.repo.RefreshTokenRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenRefreshCleanupService implements CleanupService {

    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    @Scheduled(cron = "0 30 12 * * *")
    public long purgeData() {
        return purgeExpiredTokens();
    }

    private long purgeExpiredTokens() {
        try {
            Date now = new Date();
            int deletedCount = refreshTokenRepo.deleteExpiredTokens(now);
            log.debug("Purged {} expired tokens at {}", deletedCount, now);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error purging expired tokens {}", e.getMessage(), e);
            return 0;
        }
    }
}
