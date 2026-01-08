package ru.viktorgezz.k1_typing_backend.domain.result_item.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.result_item.dto.rs.ResultListItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.repo.ResultItemRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.service.intrf.ResultItemQueryService;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("ResultItemQueryService Integration Tests")
class ResultItemQueryServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultItemQueryService resultItemQueryService;

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
    private ResultItem resultItemExisting;

    @BeforeEach
    void setUp() {
        userAuthenticated = userRepo.save(
                new User(
                        "resultQueryTestUser",
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

        Contest contestExisting = contestRepo.save(
                new Contest(Status.FINISHED, 1, exerciseExisting)
        );

        resultItemExisting = resultItemRepo.save(
                new ResultItem(
                        120L,
                        85,
                        new BigDecimal("95.50"),
                        Place.WITHOUT_PLACE,
                        contestExisting,
                        userAuthenticated
                )
        );

        setSecurityContext(userAuthenticated);
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
    @DisplayName("findAllByUser")
    class FindAllByUserTests {

        @Test
        @DisplayName("Возврат страницы с результатами при наличии данных")
        void findAllByUser_ShouldReturnPageWithResults_WhenResultsExist() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            assertThat(pageResult).isNotNull();
            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Возврат пустой страницы при отсутствии результатов")
        void findAllByUser_ShouldReturnEmptyPage_WhenNoResultsExist() {
            resultItemRepo.deleteAll();
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            assertThat(pageResult).isNotNull();
            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Корректное отображение данных результата в DTO")
        void findAllByUser_ShouldReturnCorrectDtoData_WhenResultExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            ResultListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.durationSeconds()).isEqualTo(resultItemExisting.getDurationSeconds());
            assertThat(dtoFirst.speed()).isEqualTo(resultItemExisting.getSpeed());
            assertThat(dtoFirst.accuracy()).isEqualByComparingTo(resultItemExisting.getAccuracy());
            assertThat(dtoFirst.place()).isEqualTo(resultItemExisting.getPlace());
        }

        @Test
        @DisplayName("Корректная пагинация при запросе определенной страницы")
        void findAllByUser_ShouldReturnCorrectPage_WhenPageableProvided() {
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(
                            150L,
                            90,
                            new BigDecimal("98.00"),
                            Place.WITHOUT_PLACE,
                            contestSecond,
                            userAuthenticated
                    )
            );
            Pageable pageableSizeOne = PageRequest.of(0, 1);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableSizeOne);

            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getTotalElements()).isEqualTo(2);
            assertThat(pageResult.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("DTO результата содержит корректную продолжительность")
        void findAllByUser_ShouldReturnResultWithCorrectDuration_WhenResultExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            ResultListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.durationSeconds()).isEqualTo(120L);
        }

        @Test
        @DisplayName("DTO результата содержит корректную скорость")
        void findAllByUser_ShouldReturnResultWithCorrectSpeed_WhenResultExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            ResultListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.speed()).isEqualTo(85);
        }

        @Test
        @DisplayName("DTO результата содержит корректную точность")
        void findAllByUser_ShouldReturnResultWithCorrectAccuracy_WhenResultExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            ResultListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.accuracy()).isEqualByComparingTo(new BigDecimal("95.50"));
        }

        @Test
        @DisplayName("DTO результата содержит корректное место")
        void findAllByUser_ShouldReturnResultWithCorrectPlace_WhenResultExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            ResultListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.place()).isEqualTo(Place.WITHOUT_PLACE);
        }

        @Test
        @DisplayName("Возврат только результатов текущего авторизованного пользователя")
        void findAllByUser_ShouldReturnOnlyCurrentUserResults_WhenMultipleUsersHaveResults() {
            User userOther = userRepo.save(
                    new User("otherUser", "password456", Role.USER, true, false, false)
            );
            Contest contestOther = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(200L, 100, new BigDecimal("99.00"), Place.FIRST, contestOther, userOther)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            assertThat(pageResult.getTotalElements()).isEqualTo(1);
            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getContent().getFirst().durationSeconds()).isEqualTo(resultItemExisting.getDurationSeconds());
        }

        @Test
        @DisplayName("Смена контекста пользователя возвращает результаты нового пользователя")
        void findAllByUser_ShouldReturnNewUserResults_WhenSecurityContextChanged() {
            User userOther = userRepo.save(
                    new User("otherUser", "password456", Role.USER, true, false, false)
            );
            Contest contestOther = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(200L, 100, new BigDecimal("99.00"), Place.FIRST, contestOther, userOther)
            );
            setSecurityContext(userOther);
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            assertThat(pageResult.getTotalElements()).isEqualTo(1);
            assertThat(pageResult.getContent().getFirst().speed()).isEqualTo(100);
        }

        @Test
        @DisplayName("Возврат пустой страницы для пользователя без результатов при наличии результатов у других")
        void findAllByUser_ShouldReturnEmptyPage_WhenCurrentUserHasNoResultsButOthersHave() {
            User userWithoutResults = userRepo.save(
                    new User("emptyUser", "password789", Role.USER, true, false, false)
            );
            setSecurityContext(userWithoutResults);
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultListItemRsDto> pageResult = resultItemQueryService.findAllByUser(pageableDefault);

            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }
    }
}
