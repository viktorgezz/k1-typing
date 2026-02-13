package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.impl;

import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyProgress;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.websocket.AllProgressMessage.UserProgressData;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.ProgressService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void updateProgress(Long idContest, Long idUser, int progressPercent, int speed, BigDecimal accuracy) {
        UserProgressData progressData = new UserProgressData(progressPercent, speed, accuracy);
        try {
            String jsonProgress = objectMapper.writeValueAsString(progressData);
        redisTemplate.opsForHash().put(
                keyProgress(idContest),
                idUser.toString(),
                    jsonProgress
        );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize progress data for user {} in contest {}", idUser, idContest, e);
        }
    }

    @Override
    public Map<Long, UserProgressData> getProgressAll(Long idContest) {
        Map<Long, UserProgressData> progressResult = new HashMap<>();

        redisTemplate.opsForHash().entries(keyProgress(idContest))
                .forEach((key, value) -> {
                    try {
                        Long idUser = Long.parseLong(key.toString());
                        UserProgressData progressData = objectMapper.readValue(value.toString(), UserProgressData.class);
                        progressResult.put(idUser, progressData);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize progress data: {}", value, e);
                    }
                });

        return progressResult;
    }

    @Override
    public UserProgressData getProgressByUser(Long idContest, Long idUser) {
        Object progressValue = redisTemplate.opsForHash().get(keyProgress(idContest), idUser.toString());
        if (progressValue == null) {
            return new UserProgressData(0, 0, BigDecimal.ZERO);
        }

        try {
            return objectMapper.readValue(progressValue.toString(), UserProgressData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize progress data for user {} in contest {}", idUser, idContest, e);
            return new UserProgressData(0, 0, BigDecimal.ZERO);
        }
    }
}
