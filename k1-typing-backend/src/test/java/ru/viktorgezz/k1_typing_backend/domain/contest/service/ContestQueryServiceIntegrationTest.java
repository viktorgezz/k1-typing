package ru.viktorgezz.k1_typing_backend.domain.contest.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;
import testconfig.AbstractIntegrationPostgresTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты для {@link ContestQueryService}.
 */
@DisplayName("ContestQueryService Integration Tests")
class ContestQueryServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ContestQueryService contestQueryService;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private Exercise exerciseExisting;
    private Contest contestExisting;

    @BeforeEach
    void setUp() {
        User userExisting = userRepo.save(
                new User(
                        "queryTestUser",
                        "password123",
                        Role.USER,
                        true,
                        false,
                        false
                )
        );

        exerciseExisting = exerciseRepo.save(
                new Exercise("Test Exercise", "Test exercise text content", userExisting)
        );

        contestExisting = contestRepo.save(
                new Contest(Status.PROGRESS, 1, exerciseExisting)
        );
    }

    @AfterEach
    void tearDown() {
        contestRepo.deleteAll();
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("getOne")
    class GetOneTests {

        @Test
        @DisplayName("Успешное получение контеста по существующему идентификатору")
        void getOne_ShouldReturnContest_WhenContestExists() {
            Contest contestFound = contestQueryService.getOne(contestExisting.getId());

            assertThat(contestFound).isNotNull();
            assertThat(contestFound.getId()).isEqualTo(contestExisting.getId());
        }

        @Test
        @DisplayName("Выброс исключения при запросе несуществующего контеста")
        void getOne_ShouldThrowException_WhenContestNotFound() {
            Long idContestNonExistent = 999999L;

            assertThatThrownBy(() -> contestQueryService.getOne(idContestNonExistent))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException exceptionBusiness = (BusinessException) exception;
                        assertThat(exceptionBusiness.getErrorCode()).isEqualTo(ErrorCode.CONTEST_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Контест содержит корректный статус после получения")
        void getOne_ShouldReturnContestWithCorrectStatus_WhenContestExists() {
            Contest contestFound = contestQueryService.getOne(contestExisting.getId());

            assertThat(contestFound.getStatus()).isEqualTo(Status.PROGRESS);
        }

        @Test
        @DisplayName("Контест содержит корректное количество участников после получения")
        void getOne_ShouldReturnContestWithCorrectAmount_WhenContestExists() {
            Contest contestFound = contestQueryService.getOne(contestExisting.getId());

            assertThat(contestFound.getAmount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Контест связан с корректным упражнением после получения")
        void getOne_ShouldReturnContestWithLinkedExercise_WhenContestExists() {
            Contest contestFound = contestQueryService.getOne(contestExisting.getId());

            assertThat(contestFound.getExercise()).isNotNull();
            assertThat(contestFound.getExercise().getId()).isEqualTo(exerciseExisting.getId());
        }
    }
}
