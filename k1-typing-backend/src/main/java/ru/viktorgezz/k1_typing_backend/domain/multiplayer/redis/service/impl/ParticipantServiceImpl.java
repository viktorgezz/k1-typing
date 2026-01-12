package ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.impl;

import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.TTL_ROOM;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.keyParticipants;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.keyProgress;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.keyUsernames;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.AllProgressMessage.UserProgressData;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    @Override
    public void addParticipant(Long idContest, Long idUser, String username) {
        final String keySetParticipants = keyParticipants(idContest);
        final String keyHashUsernames = keyUsernames(idContest);
        final String keyHashProgress = keyProgress(idContest);

        // Используем String для надёжной сериализации в Set
        redisTemplate.opsForSet().add(keySetParticipants, idUser.toString());
        redisTemplate.opsForHash().put(keyHashUsernames, idUser.toString(), username);

        // Инициализируем прогресс с нулевыми значениями
        try {
            UserProgressData initialProgress = new UserProgressData(0, 0, BigDecimal.ZERO);
            String jsonProgress = objectMapper.writeValueAsString(initialProgress);
            redisTemplate.opsForHash().put(keyHashProgress, idUser.toString(), jsonProgress);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize initial progress for user {} in contest {}", idUser, idContest, e);
        }

        redisTemplate.expire(keySetParticipants, TTL_ROOM);
        redisTemplate.expire(keyHashUsernames, TTL_ROOM);
        redisTemplate.expire(keyHashProgress, TTL_ROOM);
    }

    @Override
    public void removeParticipant(Long idContest, Long idUser) {
        redisTemplate.opsForSet().remove(keyParticipants(idContest), idUser.toString());
        redisTemplate.opsForHash().delete(keyUsernames(idContest), idUser.toString());
        redisTemplate.opsForHash().delete(keyProgress(idContest), idUser.toString());
    }

    @Override
    public Set<Long> getParticipantIds(Long idContest) {
        return Objects.requireNonNullElse(
                        redisTemplate.opsForSet().members(keyParticipants(idContest)), Set.of()
                )
                .stream()
                .map(valueRaw ->
                        Long.parseLong(valueRaw.toString())
                )
                .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, String> getParticipantNames(Long idContest) {
        return Objects.requireNonNullElse(
                        redisTemplate.opsForHash().entries(keyUsernames(idContest)), Map.of()
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().toString()),
                        entry -> entry.getValue().toString()
                ));
    }

    @Override
    public int getParticipantsCount(Long idContest) {
        return Objects.requireNonNullElse(
                        redisTemplate.opsForSet().size(keyParticipants(idContest)),
                        0L
                )
                .intValue();
    }

    @Override
    public boolean isRoomFull(Long idContest) {
        return getParticipantsCount(idContest) >= roomService.getParticipantsMax(idContest);
    }

    @Override
    public boolean isParticipant(Long idContest, Long idUser) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(keyParticipants(idContest), idUser.toString())
        );
    }
}
