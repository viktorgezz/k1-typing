package ru.viktorgezz.coretyping.domain.contest.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.repo.ContestRepo;
import ru.viktorgezz.coretyping.domain.contest.Status;
import ru.viktorgezz.coretyping.domain.contest.dto.rq.CreationContestRqDto;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.coretyping.domain.participant.ParticipantsRepo;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;
import testconfig.AbstractIntegrationPostgresTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты для {@link ContestCommandService}.
 */
@DisplayName("ContestCommandService Integration Tests")
class ContestCommandServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ContestCommandService contestCommandService;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ParticipantsRepo participantsRepo;

    private User userAuthenticated;
    private Exercise exerciseExisting;

    @BeforeEach
    void setUp() {
        userAuthenticated = userRepo.save(
                new User(
                        "contestTestUser",
                        "password123",
                        Role.USER,
                        true,
                        false,
                        false
                )
        );

        exerciseExisting = exerciseRepo.save(
                new Exercise("Test Exercise", "Test exercise text content", userAuthenticated)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        participantsRepo.deleteAll();
        contestRepo.deleteAll();
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    private void setSecurityContext(User user) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Nested
    @DisplayName("createSingle")
    class CreateSingleTests {

        @Test
        @DisplayName("Успешное создание контеста с привязкой к авторизованному пользователю")
        void createSingle_ShouldCreateContestWithParticipant_WhenUserAuthenticated() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.PROGRESS);

            Contest contestCreated = contestCommandService.createSingle(requestValid);

            assertThat(contestCreated).isNotNull();
            assertThat(contestCreated.getId()).isNotNull();
            assertThat(contestCreated.getStatus()).isEqualTo(Status.PROGRESS);
            assertThat(contestCreated.getAmount()).isEqualTo(1);
            assertThat(contestCreated.getExercise().getId()).isEqualTo(exerciseExisting.getId());
            assertThat(contestCreated.getParticipants()).hasSize(1);
            assertThat(contestCreated.getParticipants().getFirst().getUser().getId())
                    .isEqualTo(userAuthenticated.getId());
        }

        @Test
        @DisplayName("Успешное создание контеста без авторизации с участником без пользователя")
        void createSingle_ShouldCreateContestWithNullUser_WhenUserNotAuthenticated() {
            SecurityContextHolder.clearContext();
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.PROGRESS);

            Contest contestCreated = contestCommandService.createSingle(requestValid);

            assertThat(contestCreated).isNotNull();
            assertThat(contestCreated.getId()).isNotNull();
            assertThat(contestCreated.getStatus()).isEqualTo(Status.PROGRESS);
            assertThat(contestCreated.getAmount()).isEqualTo(1);
            assertThat(contestCreated.getParticipants()).hasSize(1);
            assertThat(contestCreated.getParticipants().getFirst().getUser()).isNull();
        }

        @Test
        @DisplayName("Выброс исключения при попытке создать контест с несуществующим упражнением")
        void createSingle_ShouldThrowException_WhenExerciseNotFound() {
            setSecurityContext(userAuthenticated);
            Long idExerciseNonExistent = 999999L;
            CreationContestRqDto requestInvalid = new CreationContestRqDto(idExerciseNonExistent, Status.FINISHED);

            assertThatThrownBy(() -> contestCommandService.createSingle(requestInvalid))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException exceptionBusiness = (BusinessException) exception;
                        assertThat(exceptionBusiness.getErrorCode()).isEqualTo(ErrorCode.EXERCISE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Контест сохраняется в базе данных после создания")
        void createSingle_ShouldPersistContest_WhenCreatedSuccessfully() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED);

            Contest contestCreated = contestCommandService.createSingle(requestValid);

            assertThat(contestRepo.findById(contestCreated.getId())).isPresent();
        }

        @Test
        @DisplayName("Участник контеста сохраняется в базе данных после создания")
        void createSingle_ShouldPersistParticipant_WhenCreatedSuccessfully() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED);

            Contest contestCreated = contestCommandService.createSingle(requestValid);

            assertThat(participantsRepo.findById(contestCreated.getParticipants().getFirst().getId()))
                    .isPresent();
        }

        @Test
        @DisplayName("Контест связывается с указанным упражнением")
        void createSingle_ShouldLinkContestToExercise_WhenExerciseExists() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED);

            Contest contestCreated = contestCommandService.createSingle(requestValid);

            assertThat(contestCreated.getExercise()).isNotNull();
            assertThat(contestCreated.getExercise().getTitle()).isEqualTo(exerciseExisting.getTitle());
            assertThat(contestCreated.getExercise().getText()).isEqualTo(exerciseExisting.getText());
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Успешное удаление существующего контеста")
        void delete_ShouldRemoveContest_WhenContestExists() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED);
            Contest contestCreated = contestCommandService.createSingle(requestValid);
            Long idContestCreated = contestCreated.getId();

            Contest contestDeleted = contestCommandService.delete(idContestCreated);

            assertThat(contestDeleted).isNotNull();
            assertThat(contestDeleted.getId()).isEqualTo(idContestCreated);
            assertThat(contestRepo.findById(idContestCreated)).isEmpty();
        }

        @Test
        @DisplayName("Возврат null при попытке удалить несуществующий контест")
        void delete_ShouldReturnNull_WhenContestNotFound() {
            Long idContestNonExistent = 999999L;

            Contest contestDeleted = contestCommandService.delete(idContestNonExistent);

            assertThat(contestDeleted).isNull();
        }

        @Test
        @DisplayName("Участники контеста удаляются вместе с контестом")
        void delete_ShouldRemoveParticipants_WhenContestDeleted() {
            setSecurityContext(userAuthenticated);
            CreationContestRqDto requestValid = new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED);
            Contest contestCreated = contestCommandService.createSingle(requestValid);
            Long idParticipant = contestCreated.getParticipants().getFirst().getId();

            contestCommandService.delete(contestCreated.getId());

            assertThat(participantsRepo.findById(idParticipant)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteMany")
    class DeleteManyTests {

        @Test
        @DisplayName("Успешное удаление нескольких контестов по списку идентификаторов")
        void deleteMany_ShouldRemoveAllContests_WhenIdsProvided() {
            setSecurityContext(userAuthenticated);
            Contest contestFirst = contestCommandService.createSingle(new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED));
            Contest contestSecond = contestCommandService.createSingle(new CreationContestRqDto(exerciseExisting.getId(), Status.FINISHED));
            java.util.List<Long> idsToDelete = java.util.List.of(contestFirst.getId(), contestSecond.getId());

            contestCommandService.deleteMany(idsToDelete);

            assertThat(contestRepo.findById(contestFirst.getId())).isEmpty();
            assertThat(contestRepo.findById(contestSecond.getId())).isEmpty();
        }

        @Test
        @DisplayName("Отсутствие ошибки при удалении пустого списка контестов")
        void deleteMany_ShouldNotThrowException_WhenEmptyListProvided() {
            java.util.List<Long> idsEmpty = java.util.List.of();

            contestCommandService.deleteMany(idsEmpty);

            assertThat(contestRepo.count()).isNotNegative();
        }

        @Test
        @DisplayName("Корректная обработка списка с несуществующими идентификаторами")
        void deleteMany_ShouldNotThrowException_WhenIdsNotFound() {
            java.util.List<Long> idsNonExistent = java.util.List.of(999998L, 999999L);

            contestCommandService.deleteMany(idsNonExistent);

            assertThat(contestRepo.findById(999998L)).isEmpty();
            assertThat(contestRepo.findById(999999L)).isEmpty();
        }
    }
}
