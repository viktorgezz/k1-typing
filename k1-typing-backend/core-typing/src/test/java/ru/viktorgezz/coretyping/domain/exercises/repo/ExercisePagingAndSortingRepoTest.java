package ru.viktorgezz.coretyping.domain.exercises.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("ExercisePagingAndSortingRepo Tests")
class ExercisePagingAndSortingRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ExercisePagingAndSortingRepo exercisePagingAndSortingRepo;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private User userExisting;

    @BeforeEach
    void setUp() {
        userExisting = userRepo.save(
                new User(
                        "exerciseRepoTestUser",
                        "password123",
                        Role.USER,
                        true,
                        false,
                        false
                )
        );

        exerciseRepo.save(
                new Exercise("Test Exercise", "Test exercise text content", userExisting)
        );
    }

    @AfterEach
    void tearDown() {
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("findAll with EntityGraph")
    class FindAllWithEntityGraphTests {

        @Test
        @DisplayName("Связанный пользователь загружается вместе с упражнением благодаря EntityGraph")
        void findAll_ShouldLoadUserEagerly_WhenEntityGraphApplied() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).isNotEmpty();
            Exercise exerciseFound = pageResult.getContent().getFirst();
            assertThat(Hibernate.isInitialized(exerciseFound.getUser())).isTrue();
        }

        @Test
        @DisplayName("Данные пользователя доступны без дополнительных запросов")
        void findAll_ShouldAllowAccessToUserData_WhenEntityGraphApplied() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableDefault);

            Exercise exerciseFound = pageResult.getContent().getFirst();
            assertThat(exerciseFound.getUser().getUsername()).isEqualTo(userExisting.getUsername());
            assertThat(exerciseFound.getUser().getRole()).isEqualTo(userExisting.getRole());
        }

        @Test
        @DisplayName("Пагинация корректно работает с EntityGraph")
        void findAll_ShouldReturnCorrectPage_WhenEntityGraphAppliedWithPagination() {
            User userSecond = userRepo.save(
                    new User(
                            "secondUser",
                            "password456",
                            Role.USER,
                            true,
                            false,
                            false
                    )
            );
            exerciseRepo.save(new Exercise("Second Exercise", "Second content", userSecond));
            exerciseRepo.save(new Exercise("Third Exercise", "Third content", userExisting));
            Pageable pageableSizeTwo = PageRequest.of(0, 2);

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableSizeTwo);

            assertThat(pageResult.getContent()).hasSize(2);
            assertThat(pageResult.getTotalElements()).isEqualTo(3);
            assertThat(pageResult.getTotalPages()).isEqualTo(2);
            pageResult.getContent().forEach(exercise -> 
                assertThat(Hibernate.isInitialized(exercise.getUser())).isTrue()
            );
        }

        @Test
        @DisplayName("Сортировка корректно работает с EntityGraph")
        void findAll_ShouldReturnSortedResults_WhenEntityGraphAppliedWithSort() {
            exerciseRepo.save(new Exercise("A First Exercise", "Content A", userExisting));
            exerciseRepo.save(new Exercise("Z Last Exercise", "Content Z", userExisting));
            Pageable pageableSorted = PageRequest.of(0, 10, Sort.by("title").ascending());

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableSorted);

            assertThat(pageResult.getContent()).hasSizeGreaterThan(1);
            assertThat(pageResult.getContent().getFirst().getTitle()).isEqualTo("A First Exercise");
        }

        @Test
        @DisplayName("Возврат пустой страницы при отсутствии упражнений")
        void findAll_ShouldReturnEmptyPage_WhenNoExercisesExist() {
            exerciseRepo.deleteAll();
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Каждое упражнение на странице имеет инициализированного пользователя")
        void findAll_ShouldInitializeUserForAllExercises_WhenMultipleExercisesExist() {
            User userSecond = userRepo.save(
                    new User(
                            "anotherUser",
                            "password789",
                            Role.ADMIN,
                            true,
                            false,
                            false
                    )
            );
            exerciseRepo.save(new Exercise("Exercise by first user", "Content 1", userExisting));
            exerciseRepo.save(new Exercise("Exercise by second user", "Content 2", userSecond));
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<Exercise> pageResult = exercisePagingAndSortingRepo.findAll(pageableDefault);

            assertThat(pageResult.getContent()).hasSize(3);
            pageResult.getContent().forEach(exercise -> {
                assertThat(Hibernate.isInitialized(exercise.getUser())).isTrue();
                assertThat(exercise.getUser().getUsername()).isNotNull();
            });
        }
    }
}
