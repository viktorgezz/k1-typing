package ru.viktorgezz.coretyping.domain.multiplayer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.FIELD_EXERCISE_ID;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.FIELD_MAX_PARTICIPANTS;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyFinishers;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyInfo;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyParticipants;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyProgress;
import static ru.viktorgezz.coretyping.domain.multiplayer.redis.utility.RedisKeyGenerator.keyUsernames;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import ru.viktorgezz.coretyping.domain.multiplayer.redis.service.intrf.RoomService;
import testconfig.AbstractIntegrationRedisTest;

class RoomServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long ID_CONTEST_TEST = 1L;
    private static final Long ID_EXERCISE_TEST = 100L;
    private static final int PARTICIPANTS_MAX_TEST = 4;

    @AfterEach
    void cleanupRedis() {
        redisTemplate.delete(keyInfo(ID_CONTEST_TEST));
        redisTemplate.delete(keyParticipants(ID_CONTEST_TEST));
        redisTemplate.delete(keyUsernames(ID_CONTEST_TEST));
        redisTemplate.delete(keyProgress(ID_CONTEST_TEST));
        redisTemplate.delete(keyFinishers(ID_CONTEST_TEST));
    }

    @Test
    @DisplayName("Создание комнаты сохраняет данные в Redis с корректными полями")
    void createRoom_ShouldStoreRoomDataInRedis_WhenValidParametersProvided() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        String keyRoom = keyInfo(ID_CONTEST_TEST);
        Object idExerciseStored = redisTemplate.opsForHash().get(keyRoom, FIELD_EXERCISE_ID);
        Object participantsMaxStored = redisTemplate.opsForHash().get(keyRoom, FIELD_MAX_PARTICIPANTS);

        assertThat(idExerciseStored).isNotNull();
        assertThat(Long.parseLong(idExerciseStored.toString())).isEqualTo(ID_EXERCISE_TEST);
        assertThat(participantsMaxStored).isNotNull();
        assertThat(Integer.parseInt(participantsMaxStored.toString())).isEqualTo(PARTICIPANTS_MAX_TEST);
    }

    @Test
    @DisplayName("Проверка существования комнаты возвращает true для созданной комнаты")
    void roomExists_ShouldReturnTrue_WhenRoomWasCreated() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        boolean roomExistsResult = roomService.roomExists(ID_CONTEST_TEST);

        assertThat(roomExistsResult).isTrue();
    }

    @Test
    @DisplayName("Проверка существования комнаты возвращает false для несуществующей комнаты")
    void roomExists_ShouldReturnFalse_WhenRoomWasNotCreated() {
        Long idContestNonExistent = 999L;

        boolean roomExistsResult = roomService.roomExists(idContestNonExistent);

        assertThat(roomExistsResult).isFalse();
    }

    @Test
    @DisplayName("Получение максимального количества участников возвращает корректное значение")
    void getParticipantsMax_ShouldReturnCorrectValue_WhenRoomExists() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        int participantsMaxResult = roomService.getParticipantsMax(ID_CONTEST_TEST);

        assertThat(participantsMaxResult).isEqualTo(PARTICIPANTS_MAX_TEST);
    }

    @Test
    @DisplayName("Получение максимального количества участников возвращает ноль для несуществующей комнаты")
    void getParticipantsMax_ShouldReturnZero_WhenRoomDoesNotExist() {
        Long idContestNonExistent = 999L;

        int participantsMaxResult = roomService.getParticipantsMax(idContestNonExistent);

        assertThat(participantsMaxResult).isZero();
    }

    @Test
    @DisplayName("Удаление комнаты очищает все связанные данные из Redis")
    void deleteRoom_ShouldRemoveAllRoomData_WhenRoomExists() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);
        redisTemplate.opsForSet().add(keyParticipants(ID_CONTEST_TEST), 1L);
        redisTemplate.opsForHash().put(keyUsernames(ID_CONTEST_TEST), "1", "TestUser");
        redisTemplate.opsForHash().put(keyProgress(ID_CONTEST_TEST), "1", 50);
        redisTemplate.opsForZSet().add(keyFinishers(ID_CONTEST_TEST), 1L, System.currentTimeMillis());

        roomService.deleteRoom(ID_CONTEST_TEST);

        assertThat(redisTemplate.hasKey(keyInfo(ID_CONTEST_TEST))).isFalse();
        assertThat(redisTemplate.hasKey(keyParticipants(ID_CONTEST_TEST))).isFalse();
        assertThat(redisTemplate.hasKey(keyUsernames(ID_CONTEST_TEST))).isFalse();
        assertThat(redisTemplate.hasKey(keyProgress(ID_CONTEST_TEST))).isFalse();
        assertThat(redisTemplate.hasKey(keyFinishers(ID_CONTEST_TEST))).isFalse();
    }

    @Test
    @DisplayName("Удаление несуществующей комнаты не вызывает ошибок")
    void deleteRoom_ShouldNotThrowException_WhenRoomDoesNotExist() {
        Long idContestNonExistent = 999L;

        roomService.deleteRoom(idContestNonExistent);

        assertThat(roomService.roomExists(idContestNonExistent)).isFalse();
    }
}
