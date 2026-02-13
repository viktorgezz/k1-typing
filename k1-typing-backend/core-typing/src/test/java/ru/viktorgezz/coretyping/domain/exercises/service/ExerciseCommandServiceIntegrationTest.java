package ru.viktorgezz.coretyping.domain.exercises.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.Language;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.CreationExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.UpdatedExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseCommandService;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;
import testconfig.AbstractIntegrationPostgresTest;

/**
 * Интеграционные тесты для {@link ExerciseCommandService}.
 */
@DisplayName("ExerciseCommandService Integration Tests")
class ExerciseCommandServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ExerciseCommandService exerciseCommandService;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepo.save(
                new User(
                        "testuser",
                        "password123",
                        Role.USER,
                        true,
                        false,
                        false
                )
        );
        setSecurityContext(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    private void setSecurityContext(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("createExercise")
    class CreateExerciseTests {

        @Test
        @DisplayName("должен успешно создать упражнение")
        void shouldCreateExerciseSuccessfully() {
            // given
            CreationExerciseRqDto request = new CreationExerciseRqDto(
                    "Test Exercise Title",
                    "This is a test exercise text with minimum length",
                    Language.ENG
            );

            // when
            exerciseCommandService.create(request);

            // then
            Iterable<Exercise> exercises = exerciseRepo.findAll();
            assertThat(exercises).hasSize(1);

            Exercise savedExercise = exercises.iterator().next();
            assertThat(savedExercise.getTitle()).isEqualTo("Test Exercise Title");
            assertThat(savedExercise.getText()).isEqualTo("This is a test exercise text with minimum length");
            assertThat(savedExercise.getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("должен связать упражнение с текущим пользователем")
        void shouldLinkExerciseToCurrentUser() {
            CreationExerciseRqDto request = new CreationExerciseRqDto(
                    "Another Exercise",
                    "Another exercise text content here",
                    Language.ENG
            );

            exerciseCommandService.create(request);

            Exercise savedExercise = exerciseRepo.findAll().iterator().next();
            assertThat(savedExercise.getUser()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateExercise")
    class UpdateExerciseTests {

        @Test
        @DisplayName("должен успешно обновить упражнение")
        void shouldUpdateExerciseSuccessfully() {
            Exercise existingExercise = exerciseRepo.save(
                    new Exercise("Original Title", "Original text content", testUser)
            );

            UpdatedExerciseRqDto request = new UpdatedExerciseRqDto(
                    existingExercise.getId(),
                    "Updated Title",
                    "Updated text content here"
            );

            exerciseCommandService.update(request);

            Optional<Exercise> updatedExercise = exerciseRepo.findById(existingExercise.getId());
            assertThat(updatedExercise).isPresent();
            assertThat(updatedExercise.get().getTitle()).isEqualTo("Updated Title");
            assertThat(updatedExercise.get().getText()).isEqualTo("Updated text content here");
        }

        @Test
        @DisplayName("должен выбросить исключение если упражнение не найдено")
        void shouldThrowExceptionWhenExerciseNotFound() {
            Long nonExistentId = 999999L;
            UpdatedExerciseRqDto request = new UpdatedExerciseRqDto(
                    nonExistentId,
                    "Title",
                    "Some text content"
            );

            assertThatThrownBy(() -> exerciseCommandService.update(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.EXERCISE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("должен сохранить связь с пользователем после обновления")
        void shouldPreserveUserLinkAfterUpdate() {
            Exercise existingExercise = exerciseRepo.save(
                    new Exercise("Original Title", "Original text content", testUser)
            );

            UpdatedExerciseRqDto request = new UpdatedExerciseRqDto(
                    existingExercise.getId(),
                    "New Title",
                    "New text content here"
            );

            exerciseCommandService.update(request);

            Optional<Exercise> updatedExercise = exerciseRepo.findById(existingExercise.getId());
            assertThat(updatedExercise).isPresent();
            assertThat(updatedExercise.get().getUser().getId()).isEqualTo(testUser.getId());
        }
    }

    @Nested
    @DisplayName("deleteExercise")
    class DeleteExerciseTests {

        @Test
        @DisplayName("должен успешно удалить упражнение")
        void shouldDeleteExerciseSuccessfully() {
            Exercise existingExercise = exerciseRepo.save(
                    new Exercise("To Delete", "Content to delete here", testUser)
            );
            Long exerciseId = existingExercise.getId();

            exerciseCommandService.delete(exerciseId);

            Optional<Exercise> deletedExercise = exerciseRepo.findById(exerciseId);
            assertThat(deletedExercise).isEmpty();
        }

        @Test
        @DisplayName("не должен выбрасывать исключение при удалении несуществующего упражнения")
        void shouldNotThrowExceptionWhenDeletingNonExistent() {
            Long nonExistentId = 999999L;

            exerciseCommandService.delete(nonExistentId);

            assertThat(exerciseRepo.findById(nonExistentId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteMany")
    class DeleteManyTests {

        @Test
        @DisplayName("Успешное удаление нескольких упражнений по списку идентификаторов")
        void deleteMany_ShouldRemoveAllExercises_WhenIdsProvided() {
            Exercise exerciseFirst = exerciseRepo.save(
                    new Exercise("First Exercise", "First exercise content text", testUser)
            );
            Exercise exerciseSecond = exerciseRepo.save(
                    new Exercise("Second Exercise", "Second exercise content text", testUser)
            );
            java.util.List<Long> idsToDelete = java.util.List.of(exerciseFirst.getId(), exerciseSecond.getId());

            exerciseCommandService.deleteMany(idsToDelete);

            assertThat(exerciseRepo.findById(exerciseFirst.getId())).isEmpty();
            assertThat(exerciseRepo.findById(exerciseSecond.getId())).isEmpty();
        }

        @Test
        @DisplayName("Отсутствие ошибки при удалении пустого списка упражнений")
        void deleteMany_ShouldNotThrowException_WhenEmptyListProvided() {
            java.util.List<Long> idsEmpty = java.util.List.of();

            exerciseCommandService.deleteMany(idsEmpty);

            assertThat(exerciseRepo.count()).isNotNegative();
        }

        @Test
        @DisplayName("Корректная обработка списка с несуществующими идентификаторами")
        void deleteMany_ShouldNotThrowException_WhenIdsNotFound() {
            java.util.List<Long> idsNonExistent = java.util.List.of(999998L, 999999L);

            exerciseCommandService.deleteMany(idsNonExistent);

            assertThat(exerciseRepo.findById(999998L)).isEmpty();
            assertThat(exerciseRepo.findById(999999L)).isEmpty();
        }
    }
}
