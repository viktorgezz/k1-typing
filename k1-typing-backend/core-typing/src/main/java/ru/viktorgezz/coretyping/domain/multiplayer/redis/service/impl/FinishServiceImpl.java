package ru.viktorgezz.coretyping.domain.multiplayer.redis.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.FinishService;
import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.coretyping.domain.result_item.Place;

import java.util.Objects;

import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.TTL_ROOM;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyFinishers;

@Service
@RequiredArgsConstructor
public class FinishServiceImpl implements FinishService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ParticipantsService participantsService;

    @Override
    public Place registerFinish(Long idContest, Long idUser) {
        final String keySortedFinishers = keyFinishers(idContest);
        double timestampFinish = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(keySortedFinishers, idUser, timestampFinish);
        redisTemplate.expire(keySortedFinishers, TTL_ROOM);

        Long rankPosition = redisTemplate.opsForZSet().rank(keySortedFinishers, idUser);

        return determinePlace(rankPosition);
    }

    @Override
    public int getFinishersCount(Long idContest) {
        return Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForZSet().size(keyFinishers(idContest))).toString());
    }

    @Override
    public boolean isContestComplete(Long idContest) {
        int countParticipants = participantsService.getParticipantsCount(idContest);
        return getFinishersCount(idContest) >= countParticipants && countParticipants > 0;
    }

    private Place determinePlace(Long rankPosition) {
        if (rankPosition == null) {
            return Place.WITHOUT_PLACE;
        }

        return switch (rankPosition.intValue()) {
            case 0 -> Place.FIRST;
            case 1 -> Place.SECOND;
            case 2 -> Place.THIRD;
            default -> Place.WITHOUT_PLACE;
        };
    }
}
