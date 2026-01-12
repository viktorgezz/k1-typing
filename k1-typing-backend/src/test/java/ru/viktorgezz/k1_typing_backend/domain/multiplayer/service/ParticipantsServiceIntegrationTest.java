package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.utility.RedisKeyGenerator.keyProgress;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.AllProgressMessage;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import testconfig.AbstractIntegrationRedisTest;

@Slf4j
class ParticipantsServiceIntegrationTest extends AbstractIntegrationRedisTest {

    @Autowired
    private ParticipantsService participantsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomService roomService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long ID_CONTEST_TEST = 2L;
    private static final Long ID_EXERCISE_TEST = 100L;
    private static final int PARTICIPANTS_MAX_TEST = 4;

    private static final Long ID_USER_FIRST = 10L;
    private static final Long ID_USER_SECOND = 20L;
    private static final String USERNAME_FIRST = "PlayerFirst";
    private static final String USERNAME_SECOND = "PlayerSecond";

    @BeforeEach
    void setupRoom() {
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);
    }

    @AfterEach
    void cleanupRedis() {
        roomService.deleteRoom(ID_CONTEST_TEST);
    }

    @Test
    @DisplayName("Добавление участника сохраняет его в множество участников комнаты")
    void addParticipant_ShouldAddUserToParticipantsSet_WhenValidDataProvided() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        Set<Long> idsParticipant = participantsService.getParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipant).contains(ID_USER_FIRST);
    }

    @Test
    @DisplayName("Добавление участника сохраняет его имя в хеш имён")
    void addParticipant_ShouldStoreUserNameInHash_WhenValidDataProvided() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        Map<Long, String> namesParticipant = participantsService.getParticipantNames(ID_CONTEST_TEST);

        assertThat(namesParticipant).containsEntry(ID_USER_FIRST, USERNAME_FIRST);
    }

    @Test
    @DisplayName("Добавление участника инициализирует его прогресс нулём")
    void addParticipant_ShouldInitializeProgressToZero_WhenUserAdded() throws IOException {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        AllProgressMessage.UserProgressData progressStored = objectMapper.readValue(
                Objects.requireNonNull(redisTemplate.opsForHash().get(keyProgress(ID_CONTEST_TEST), ID_USER_FIRST.toString()))
                        .toString(),
                AllProgressMessage.UserProgressData.class);
        assertThat(progressStored).isNotNull();
        assertThat(progressStored.progress()).isZero();
    }

    @Test
    @DisplayName("Удаление участника убирает его из всех структур данных")
    void removeParticipant_ShouldRemoveUserFromAllDataStructures_WhenUserExists() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        participantsService.removeParticipant(ID_CONTEST_TEST, ID_USER_FIRST);

        Set<Long> idsParticipant = participantsService.getParticipantIds(ID_CONTEST_TEST);
        Map<Long, String> namesParticipant = participantsService.getParticipantNames(ID_CONTEST_TEST);

        assertThat(idsParticipant).isEmpty();
        assertThat(namesParticipant).doesNotContainKey(ID_USER_FIRST);
    }

    @Test
    @DisplayName("Получение идентификаторов участников возвращает всех добавленных пользователей")
    void getParticipantIds_ShouldReturnAllAddedUsers_WhenMultipleParticipantsExist() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);

        Set<Long> idsParticipant = participantsService.getParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipant).containsExactlyInAnyOrder(ID_USER_FIRST, ID_USER_SECOND);
    }

    @Test
    @DisplayName("Получение идентификаторов участников возвращает пустое множество для пустой комнаты")
    void getParticipantIds_ShouldReturnEmptySet_WhenNoParticipantsExist() {
        Set<Long> idsParticipant = participantsService.getParticipantIds(ID_CONTEST_TEST);

        assertThat(idsParticipant).isEmpty();
    }

    @Test
    @DisplayName("Получение имён участников возвращает корректную карту имён")
    void getParticipantNames_ShouldReturnCorrectNameMap_WhenParticipantsExist() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);

        Map<Long, String> namesParticipant = participantsService.getParticipantNames(ID_CONTEST_TEST);

        assertThat(namesParticipant)
                .hasSize(2)
                .containsEntry(ID_USER_FIRST, USERNAME_FIRST)
                .containsEntry(ID_USER_SECOND, USERNAME_SECOND);
    }

    @Test
    @DisplayName("Подсчёт участников возвращает корректное количество")
    void getParticipantsCount_ShouldReturnCorrectCount_WhenParticipantsExist() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);

        int countParticipants = participantsService.getParticipantsCount(ID_CONTEST_TEST);

        assertThat(countParticipants).isEqualTo(2);
    }

    @Test
    @DisplayName("Подсчёт участников возвращает ноль для пустой комнаты")
    void getParticipantsCount_ShouldReturnZero_WhenNoParticipantsExist() {
        int countParticipants = participantsService.getParticipantsCount(ID_CONTEST_TEST);

        assertThat(countParticipants).isZero();
    }

    @Test
    @DisplayName("Проверка заполненности комнаты возвращает true когда достигнут максимум")
    void isRoomFull_ShouldReturnTrue_WhenParticipantsCountEqualsMax() {
        roomService.deleteRoom(ID_CONTEST_TEST);
        int participantsMaxSmall = 2;
        roomService.createRoom(ID_CONTEST_TEST, ID_EXERCISE_TEST, participantsMaxSmall);

        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);

        boolean isRoomFullResult = participantsService.isRoomFull(ID_CONTEST_TEST);

        assertThat(isRoomFullResult).isTrue();
    }

    @Test
    @DisplayName("Проверка заполненности комнаты возвращает false когда есть свободные места")
    void isRoomFull_ShouldReturnFalse_WhenRoomHasAvailableSlots() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        boolean isRoomFullResult = participantsService.isRoomFull(ID_CONTEST_TEST);

        assertThat(isRoomFullResult).isFalse();
    }

    @Test
    @DisplayName("Проверка участия возвращает true для добавленного участника")
    void isParticipant_ShouldReturnTrue_WhenUserIsAdded() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);

        boolean isParticipantResult = participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(isParticipantResult).isTrue();
    }

    @Test
    @DisplayName("Проверка участия возвращает false для недобавленного участника")
    void isParticipant_ShouldReturnFalse_WhenUserIsNotAdded() {
        boolean isParticipantResult = participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST);

        assertThat(isParticipantResult).isFalse();
    }

    @Test
    @DisplayName("Проверка участия возвращает false после удаления участника")
    void isParticipant_ShouldReturnFalse_WhenUserIsRemoved() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        assertThat(participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST)).isTrue();

        participantsService.removeParticipant(ID_CONTEST_TEST, ID_USER_FIRST);

        boolean isParticipantResult = participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST);
        assertThat(isParticipantResult).isFalse();
    }

    @Test
    @DisplayName("Проверка участия различает разных участников в одной комнате")
    void isParticipant_ShouldDistinguishDifferentUsers_WhenMultipleParticipantsExist() {
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_SECOND, USERNAME_SECOND);

        boolean isParticipantFirst = participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST);
        boolean isParticipantSecond = participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_SECOND);

        assertThat(isParticipantFirst).isTrue();
        assertThat(isParticipantSecond).isTrue();
    }

    @Test
    @DisplayName("Проверка участия изолирована между разными соревнованиями")
    void isParticipant_ShouldIsolateParticipantsByContest_WhenDifferentContests() {
        Long idContestOther = 999L;
        roomService.createRoom(idContestOther, ID_EXERCISE_TEST, PARTICIPANTS_MAX_TEST);

        participantsService.addParticipant(ID_CONTEST_TEST, ID_USER_FIRST, USERNAME_FIRST);
        participantsService.addParticipant(idContestOther, ID_USER_SECOND, USERNAME_SECOND);

        assertThat(participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_FIRST)).isTrue();
        assertThat(participantsService.isParticipant(ID_CONTEST_TEST, ID_USER_SECOND)).isFalse();
        assertThat(participantsService.isParticipant(idContestOther, ID_USER_FIRST)).isFalse();
        assertThat(participantsService.isParticipant(idContestOther, ID_USER_SECOND)).isTrue();

        roomService.deleteRoom(idContestOther);
    }
}
