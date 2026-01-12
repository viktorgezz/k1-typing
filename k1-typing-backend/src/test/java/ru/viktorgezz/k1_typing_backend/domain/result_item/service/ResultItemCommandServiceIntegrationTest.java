package ru.viktorgezz.k1_typing_backend.domain.result_item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.repo.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rq.ResulItemRqDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.RecordedResulItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemCommandService;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("ResultItemCommandService Integration Tests")
class ResultItemCommandServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultItemCommandService resultItemCommandService;

    @Autowired
    private ResultItemRepo resultItemRepo;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private User userAuthenticated;
    private Exercise exerciseExisting;

    @BeforeEach
    void setUp() {
        userAuthenticated = userRepo.save(
                new User(
                        "resultItemTestUser",
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

        contestRepo.save(
                new Contest(Status.PROGRESS, 1, exerciseExisting)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        resultItemRepo.deleteAll();
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
    @DisplayName("saveResultSingleContest")
    class SaveResultSingleContestTests {

        @Test
        @DisplayName("Успешное сохранение результата контеста с авторизованным пользователем")
        void saveResultSingleContest_ShouldSaveResult_WhenUserAuthenticated() {
            setSecurityContext(userAuthenticated);
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    120L,
                    85,
                    new BigDecimal("95.50"),
                    exerciseExisting.getId()
            );

            RecordedResulItemRsDto resultRecorded = resultItemCommandService.saveResultSingleContest(requestValid);

            assertThat(resultRecorded).isNotNull();
            assertThat(resultRecorded.durationSeconds()).isEqualTo(120L);
            assertThat(resultRecorded.speed()).isEqualTo(85);
            assertThat(resultRecorded.accuracy()).isEqualByComparingTo(new BigDecimal("95.50"));
            assertThat(resultRecorded.place()).isEqualTo(Place.WITHOUT_PLACE);
        }

        @Test
        @DisplayName("Успешное сохранение результата контеста без авторизации")
        void saveResultSingleContest_ShouldSaveResultWithNullUser_WhenUserNotAuthenticated() {
            SecurityContextHolder.clearContext();
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    100L,
                    70,
                    new BigDecimal("88.00"),
                    exerciseExisting.getId()
            );

            RecordedResulItemRsDto resultRecorded = resultItemCommandService.saveResultSingleContest(requestValid);

            assertThat(resultRecorded).isNotNull();
            assertThat(resultRecorded.durationSeconds()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Результат сохраняется в базе данных после создания")
        void saveResultSingleContest_ShouldPersistResultItem_WhenCreatedSuccessfully() {
            setSecurityContext(userAuthenticated);
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    180L,
                    100,
                    new BigDecimal("100.00"),
                    exerciseExisting.getId()
            );

            resultItemCommandService.saveResultSingleContest(requestValid);

            Iterable<ResultItem> resultsAll = resultItemRepo.findAll();
            assertThat(resultsAll).hasSize(1);
        }

        @Test
        @DisplayName("Выброс исключения при сохранении результата для несуществующего упражнения")
        void saveResultSingleContest_ShouldThrowException_WhenExerciseNotFound() {
            setSecurityContext(userAuthenticated);
            Long idExerciseNonExistent = 999999L;
            ResulItemRqDto requestInvalid = new ResulItemRqDto(
                    120L,
                    85,
                    new BigDecimal("95.50"),
                    idExerciseNonExistent
            );

            assertThatThrownBy(() -> resultItemCommandService.saveResultSingleContest(requestInvalid))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException exceptionBusiness = (BusinessException) exception;
                        assertThat(exceptionBusiness.getErrorCode()).isEqualTo(ErrorCode.EXERCISE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Результат связывается с созданным контестом")
        void saveResultSingleContest_ShouldLinkResultToCreatedContest_WhenExerciseExists() {
            setSecurityContext(userAuthenticated);
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    200L,
                    110,
                    new BigDecimal("97.25"),
                    exerciseExisting.getId()
            );

            resultItemCommandService.saveResultSingleContest(requestValid);

            ResultItem resultItemSaved = resultItemRepo.findAll().iterator().next();
            assertThat(resultItemSaved.getContest()).isNotNull();
        }

        @Test
        @DisplayName("Результат связывается с текущим пользователем при авторизации")
        void saveResultSingleContest_ShouldLinkResultToUser_WhenUserAuthenticated() {
            setSecurityContext(userAuthenticated);
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    130L,
                    75,
                    new BigDecimal("85.00"),
                    exerciseExisting.getId()
            );

            resultItemCommandService.saveResultSingleContest(requestValid);

            ResultItem resultItemSaved = resultItemRepo.findAll().iterator().next();
            assertThat(resultItemSaved.getUser()).isNotNull();
            assertThat(resultItemSaved.getUser().getId()).isEqualTo(userAuthenticated.getId());
        }

        @Test
        @DisplayName("Результат всегда создается с местом WITHOUT_PLACE для одиночного контеста")
        void saveResultSingleContest_ShouldSetPlaceWithoutPlace_WhenResultCreated() {
            setSecurityContext(userAuthenticated);
            ResulItemRqDto requestValid = new ResulItemRqDto(
                    140L,
                    80,
                    new BigDecimal("90.00"),
                    exerciseExisting.getId()
            );

            RecordedResulItemRsDto resultRecorded = resultItemCommandService.saveResultSingleContest(requestValid);

            assertThat(resultRecorded.place()).isEqualTo(Place.WITHOUT_PLACE);
        }
    }
}
