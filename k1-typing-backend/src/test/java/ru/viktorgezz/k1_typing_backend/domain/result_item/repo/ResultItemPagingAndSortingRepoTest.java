package ru.viktorgezz.k1_typing_backend.domain.result_item.repo;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResultItemPagingAndSortingRepo Tests")
class ResultItemPagingAndSortingRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultItemPagingAndSortingRepo resultItemPagingAndSortingRepo;

    @Autowired
    private ResultItemRepo resultItemRepo;

    @Autowired
    private ContestRepo contestRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private User userExisting;
    private Exercise exerciseExisting;
    private Contest contestExisting;

    @BeforeEach
    void setUp() {
        userExisting = userRepo.save(
                new User(
                        "resultItemRepoTestUser",
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
                new Contest(Status.FINISHED, 1, exerciseExisting)
        );

        resultItemRepo.save(
                new ResultItem(
                        120L,
                        85,
                        new BigDecimal("95.50"),
                        Place.WITHOUT_PLACE,
                        contestExisting,
                        userExisting
                )
        );
    }

    @AfterEach
    void tearDown() {
        resultItemRepo.deleteAll();
        contestRepo.deleteAll();
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("findAll with EntityGraph")
    class FindAllWithEntityGraphTests {

        @Test
        @DisplayName("Связанный пользователь загружается вместе с результатом благодаря EntityGraph")
        void findAll_ShouldLoadUserEagerly_WhenEntityGraphApplied() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).isNotEmpty();
            ResultItem resultItemFound = pageResult.getContent().getFirst();
            assertThat(Hibernate.isInitialized(resultItemFound.getUser())).isTrue();
        }

        @Test
        @DisplayName("Данные пользователя доступны без дополнительных запросов")
        void findAll_ShouldAllowAccessToUserData_WhenEntityGraphApplied() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableDefault);

            ResultItem resultItemFound = pageResult.getContent().getFirst();
            assertThat(resultItemFound.getUser().getUsername()).isEqualTo(userExisting.getUsername());
            assertThat(resultItemFound.getUser().getRole()).isEqualTo(userExisting.getRole());
        }

        @Test
        @DisplayName("Пагинация корректно работает с EntityGraph")
        void findAll_ShouldReturnCorrectPage_WhenEntityGraphAppliedWithPagination() {
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(150L, 90, new BigDecimal("98.00"), Place.FIRST, contestSecond, userExisting)
            );
            resultItemRepo.save(
                    new ResultItem(180L, 75, new BigDecimal("88.00"), Place.SECOND, contestExisting, userExisting)
            );
            Pageable pageableSizeTwo = PageRequest.of(0, 2);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableSizeTwo);

            assertThat(pageResult.getContent()).hasSize(2);
            assertThat(pageResult.getTotalElements()).isEqualTo(3);
            assertThat(pageResult.getTotalPages()).isEqualTo(2);
            pageResult.getContent().forEach(resultItem ->
                    assertThat(Hibernate.isInitialized(resultItem.getUser())).isTrue()
            );
        }

        @Test
        @DisplayName("Сортировка по скорости корректно работает с EntityGraph")
        void findAll_ShouldReturnSortedBySpeed_WhenEntityGraphAppliedWithSort() {
            resultItemRepo.save(
                    new ResultItem(100L, 50, new BigDecimal("70.00"), Place.WITHOUT_PLACE, contestExisting, userExisting)
            );
            resultItemRepo.save(
                    new ResultItem(90L, 120, new BigDecimal("99.00"), Place.WITHOUT_PLACE, contestExisting, userExisting)
            );
            Pageable pageableSorted = PageRequest.of(0, 10, Sort.by("speed").descending());

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableSorted);

            assertThat(pageResult.getContent()).hasSizeGreaterThan(1);
            assertThat(pageResult.getContent().getFirst().getSpeed()).isEqualTo(120);
        }

        @Test
        @DisplayName("Возврат пустой страницы при отсутствии результатов")
        void findAll_ShouldReturnEmptyPage_WhenNoResultsExist() {
            resultItemRepo.deleteAll();
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Каждый результат на странице имеет инициализированного пользователя")
        void findAll_ShouldInitializeUserForAllResults_WhenMultipleResultsExist() {
            User userSecond = userRepo.save(
                    new User(
                            "anotherResultUser",
                            "password789",
                            Role.ADMIN,
                            true,
                            false,
                            false
                    )
            );
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(200L, 100, new BigDecimal("100.00"), Place.FIRST, contestSecond, userSecond)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).hasSize(2);
            pageResult.getContent().forEach(resultItem -> {
                assertThat(Hibernate.isInitialized(resultItem.getUser())).isTrue();
                assertThat(resultItem.getUser().getUsername()).isNotNull();
            });
        }

        @Test
        @DisplayName("Результат с null пользователем корректно обрабатывается")
        void findAll_ShouldHandleNullUser_WhenResultHasNoUser() {
            Contest contestAnonymous = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(130L, 80, new BigDecimal("85.00"), Place.WITHOUT_PLACE, contestAnonymous, null)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).hasSize(2);
            long countResultsWithNullUser = pageResult.getContent().stream()
                    .filter(r -> r.getUser() == null)
                    .count();
            assertThat(countResultsWithNullUser).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("Возврат только результатов указанного пользователя")
        void findByUserId_ShouldReturnOnlyUserResults_WhenUserIdProvided() {
            User userSecond = userRepo.save(
                    new User("anotherUser", "password456", Role.USER, true, false, false)
            );
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(200L, 100, new BigDecimal("99.00"), Place.FIRST, contestSecond, userSecond)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(userExisting.getId(), pageableDefault);

            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getContent().getFirst().getUser().getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Связанный пользователь загружается благодаря EntityGraph")
        void findByUserId_ShouldLoadUserEagerly_WhenEntityGraphApplied() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(userExisting.getId(), pageableDefault);

            assertThat(pageResult.getContent()).isNotEmpty();
            ResultItem resultItemFound = pageResult.getContent().getFirst();
            assertThat(Hibernate.isInitialized(resultItemFound.getUser())).isTrue();
        }

        @Test
        @DisplayName("Возврат пустой страницы при отсутствии результатов у пользователя")
        void findByUserId_ShouldReturnEmptyPage_WhenUserHasNoResults() {
            User userWithoutResults = userRepo.save(
                    new User("emptyUser", "password789", Role.USER, true, false, false)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(userWithoutResults.getId(), pageableDefault);

            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Возврат пустой страницы при несуществующем идентификаторе пользователя")
        void findByUserId_ShouldReturnEmptyPage_WhenUserIdNotExists() {
            Long idUserNonExistent = 999999L;
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(idUserNonExistent, pageableDefault);

            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Пагинация корректно работает при фильтрации по пользователю")
        void findByUserId_ShouldReturnCorrectPage_WhenPaginationApplied() {
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(150L, 90, new BigDecimal("98.00"), Place.FIRST, contestSecond, userExisting)
            );
            resultItemRepo.save(
                    new ResultItem(180L, 75, new BigDecimal("88.00"), Place.SECOND, contestExisting, userExisting)
            );
            Pageable pageableSizeTwo = PageRequest.of(0, 2);

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(userExisting.getId(), pageableSizeTwo);

            assertThat(pageResult.getContent()).hasSize(2);
            assertThat(pageResult.getTotalElements()).isEqualTo(3);
            assertThat(pageResult.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Сортировка корректно работает при фильтрации по пользователю")
        void findByUserId_ShouldReturnSortedResults_WhenSortApplied() {
            resultItemRepo.save(
                    new ResultItem(100L, 50, new BigDecimal("70.00"), Place.WITHOUT_PLACE, contestExisting, userExisting)
            );
            resultItemRepo.save(
                    new ResultItem(90L, 120, new BigDecimal("99.00"), Place.WITHOUT_PLACE, contestExisting, userExisting)
            );
            Pageable pageableSorted = PageRequest.of(0, 10, Sort.by("speed").descending());

            Page<ResultItem> pageResult = resultItemPagingAndSortingRepo.findByUserId(userExisting.getId(), pageableSorted);

            assertThat(pageResult.getContent()).hasSizeGreaterThan(1);
            assertThat(pageResult.getContent().getFirst().getSpeed()).isEqualTo(120);
        }

        @Test
        @DisplayName("Корректный подсчет общего количества результатов пользователя")
        void findByUserId_ShouldReturnCorrectTotalElements_WhenMultipleUsersHaveResults() {
            User userSecond = userRepo.save(
                    new User("secondUser", "password456", Role.USER, true, false, false)
            );
            Contest contestSecond = contestRepo.save(
                    new Contest(Status.FINISHED, 1, exerciseExisting)
            );
            resultItemRepo.save(
                    new ResultItem(200L, 100, new BigDecimal("99.00"), Place.FIRST, contestSecond, userSecond)
            );
            resultItemRepo.save(
                    new ResultItem(150L, 90, new BigDecimal("95.00"), Place.SECOND, contestExisting, userSecond)
            );
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ResultItem> pageResultFirst = resultItemPagingAndSortingRepo.findByUserId(userExisting.getId(), pageableDefault);
            Page<ResultItem> pageResultSecond = resultItemPagingAndSortingRepo.findByUserId(userSecond.getId(), pageableDefault);

            assertThat(pageResultFirst.getTotalElements()).isEqualTo(1);
            assertThat(pageResultSecond.getTotalElements()).isEqualTo(2);
        }
    }
}
