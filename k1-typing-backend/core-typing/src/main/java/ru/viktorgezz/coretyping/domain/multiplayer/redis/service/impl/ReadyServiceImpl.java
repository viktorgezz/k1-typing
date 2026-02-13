package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.ReadyService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.TTL_ROOM;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyReady;

@Service
@RequiredArgsConstructor
public class ReadyServiceImpl implements ReadyService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void markReady(Long idContest, Long idUser) {
        String key = keyReady(idContest);
        redisTemplate.opsForSet().add(key, idUser);
        redisTemplate.expire(key, TTL_ROOM);
    }

    @Override
    public void unmarkReady(Long idContest, Long idUser) {
        redisTemplate.opsForSet().remove(keyReady(idContest), idUser);
    }

    @Override
    public boolean isReady(Long idContest, Long idUser) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(keyReady(idContest), idUser)
        );
    }

    @Override
    public Set<Long> getReadyParticipantIds(Long idContest) {
        return Objects.requireNonNullElse(
                        redisTemplate.opsForSet().members(keyReady(idContest)), Set.of()
                )
                .stream()
                .map(value -> Long.parseLong(value.toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public int getReadyCount(Long idContest) {
        return Objects.requireNonNullElse(
                redisTemplate.opsForSet().size(keyReady(idContest)),
                0L)
                .intValue();
    }

    @Override
    public void clearReady(Long idContest) {
        redisTemplate.delete(keyReady(idContest));
    }
}
