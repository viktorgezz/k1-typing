package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.RoomService;

import java.util.Objects;

import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.*;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void createRoom(Long idContest, Long idExercise, int participantsMax) {
        final String keyRoomInfo = keyInfo(idContest);

        redisTemplate.opsForHash().put(keyRoomInfo, FIELD_EXERCISE_ID, idExercise);
        redisTemplate.opsForHash().put(keyRoomInfo, FIELD_MAX_PARTICIPANTS, participantsMax);

        redisTemplate.expire(keyRoomInfo, TTL_ROOM);
    }

    @Override
    public void deleteRoom(Long idContest) {
        redisTemplate.delete(keyInfo(idContest));
        redisTemplate.delete(keyParticipants(idContest));
        redisTemplate.delete(keyUsernames(idContest));
        redisTemplate.delete(keyProgress(idContest));
        redisTemplate.delete(keyFinishers(idContest));
    }

    @Override
    public boolean roomExists(Long idContest) {
        return Objects.requireNonNullElse(redisTemplate.hasKey(keyInfo(idContest)), false);
    }

    @Override
    public int getParticipantsMax(Long idContest) {
        return Integer.parseInt(
                Objects.requireNonNullElse(
                                redisTemplate.opsForHash().get(keyInfo(idContest), FIELD_MAX_PARTICIPANTS),
                                "0")
                        .toString()
        );
    }
}
