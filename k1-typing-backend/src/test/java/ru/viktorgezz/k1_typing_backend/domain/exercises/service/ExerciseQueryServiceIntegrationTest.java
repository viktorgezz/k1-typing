package ru.viktorgezz.k1_typing_backend.domain.exercises.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rs.ExerciseGetByIdRsDto;
import ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rs.ExerciseListItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;
import testconfig.AbstractIntegrationPostgresTest;

/**
 * Интеграционные тесты для {@link ExerciseQueryService}.
 */
@DisplayName("ExerciseQueryService Integration Tests")
class ExerciseQueryServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ExerciseQueryService exerciseQueryService;

    @Autowired
    private ExerciseRepo exerciseRepo;

    @Autowired
    private UserRepo userRepo;

    private User userExisting;
    private Exercise exerciseExisting;

    @BeforeEach
    void setUp() {
        userExisting = userRepo.save(
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
    }

    @AfterEach
    void tearDown() {
        exerciseRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Возврат страницы с упражнениями при наличии данных")
        void findAll_ShouldReturnPageWithExercises_WhenExercisesExist() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ExerciseListItemRsDto> pageResult = exerciseQueryService.findAll(pageableDefault);

            assertThat(pageResult).isNotNull();
            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Возврат пустой страницы при отсутствии упражнений")
        void findAll_ShouldReturnEmptyPage_WhenNoExercisesExist() {
            exerciseRepo.deleteAll();
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ExerciseListItemRsDto> pageResult = exerciseQueryService.findAll(pageableDefault);

            assertThat(pageResult).isNotNull();
            assertThat(pageResult.getContent()).isEmpty();
            assertThat(pageResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Корректное отображение данных упражнения в DTO списка")
        void findAll_ShouldReturnCorrectDtoData_WhenExerciseExists() {
            Pageable pageableDefault = PageRequest.of(0, 10);

            Page<ExerciseListItemRsDto> pageResult = exerciseQueryService.findAll(pageableDefault);

            ExerciseListItemRsDto dtoFirst = pageResult.getContent().getFirst();
            assertThat(dtoFirst.id()).isEqualTo(exerciseExisting.getId());
            assertThat(dtoFirst.title()).isEqualTo(exerciseExisting.getTitle());
            assertThat(dtoFirst.nameTile()).isEqualTo(userExisting.getUsername());
        }

        @Test
        @DisplayName("Корректная пагинация при запросе определенной страницы")
        void findAll_ShouldReturnCorrectPage_WhenPageableProvided() {
            exerciseRepo.save(
                    new Exercise("Second Exercise", "Second exercise text content", userExisting)
            );
            Pageable pageableSizeOne = PageRequest.of(0, 1);

            Page<ExerciseListItemRsDto> pageResult = exerciseQueryService.findAll(pageableSizeOne);

            assertThat(pageResult.getContent()).hasSize(1);
            assertThat(pageResult.getTotalElements()).isEqualTo(2);
            assertThat(pageResult.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getOneDto")
    class GetOneDtoTests {

        @Test
        @DisplayName("Успешное получение DTO упражнения по существующему идентификатору")
        void getOneDto_ShouldReturnDto_WhenExerciseExists() {
            ExerciseGetByIdRsDto dtoFound = new ExerciseGetByIdRsDto(exerciseQueryService.getOne(exerciseExisting.getId()));

            assertThat(dtoFound).isNotNull();
            assertThat(dtoFound.id()).isEqualTo(exerciseExisting.getId());
            assertThat(dtoFound.text()).isEqualTo(exerciseExisting.getText());
        }

        @Test
        @DisplayName("Выброс исключения при запросе DTO несуществующего упражнения")
        void getOneDto_ShouldThrowException_WhenExerciseNotFound() {
            Long idExerciseNonExistent = 999999L;

            assertThatThrownBy(() -> exerciseQueryService.getOne(idExerciseNonExistent))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException exceptionBusiness = (BusinessException) exception;
                        assertThat(exceptionBusiness.getErrorCode()).isEqualTo(ErrorCode.EXERCISE_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("getOne")
    class GetOneTests {

        @Test
        @DisplayName("Успешное получение упражнения по существующему идентификатору")
        void getOne_ShouldReturnExercise_WhenExerciseExists() {
            Exercise exerciseFound = exerciseQueryService.getOne(exerciseExisting.getId());

            assertThat(exerciseFound).isNotNull();
            assertThat(exerciseFound.getId()).isEqualTo(exerciseExisting.getId());
        }

        @Test
        @DisplayName("Выброс исключения при запросе несуществующего упражнения")
        void getOne_ShouldThrowException_WhenExerciseNotFound() {
            Long idExerciseNonExistent = 999999L;

            assertThatThrownBy(() -> exerciseQueryService.getOne(idExerciseNonExistent))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException exceptionBusiness = (BusinessException) exception;
                        assertThat(exceptionBusiness.getErrorCode()).isEqualTo(ErrorCode.EXERCISE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Упражнение содержит корректный заголовок и текст после получения")
        void getOne_ShouldReturnExerciseWithCorrectData_WhenExerciseExists() {
            Exercise exerciseFound = exerciseQueryService.getOne(exerciseExisting.getId());

            assertThat(exerciseFound.getTitle()).isEqualTo(exerciseExisting.getTitle());
            assertThat(exerciseFound.getText()).isEqualTo(exerciseExisting.getText());
        }

        @Test
        @DisplayName("Упражнение связано с корректным пользователем после получения")
        void getOne_ShouldReturnExerciseWithLinkedUser_WhenExerciseExists() {
            Exercise exerciseFound = exerciseQueryService.getOne(exerciseExisting.getId());

            assertThat(exerciseFound.getUser()).isNotNull();
            assertThat(exerciseFound.getUser().getId()).isEqualTo(userExisting.getId());
        }
    }
}
