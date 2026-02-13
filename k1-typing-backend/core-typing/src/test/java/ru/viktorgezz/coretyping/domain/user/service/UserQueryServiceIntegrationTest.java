package ru.viktorgezz.coretyping.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserQueryService;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("UserQueryService Integration Tests")
class UserQueryServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserRepo userRepo;

    private User userExisting;

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        userRepo.deleteAll();
    }

    private void setSecurityContext(User user) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Nested
    @DisplayName("getOne")
    class GetOneTests {

        @Test
        @DisplayName("Успешное получение пользователя по существующему идентификатору")
        void getOne_ShouldReturnUser_WhenUserExists() {
            User userFound = userQueryService.getOne(userExisting.getId());

            assertThat(userFound).isNotNull();
            assertThat(userFound.getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Выброс исключения при запросе несуществующего пользователя")
        void getOne_ShouldThrowException_WhenUserNotFound() {
            Long idUserNonExistent = 999999L;

            assertThatThrownBy(() -> userQueryService.getOne(idUserNonExistent))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("Пользователь содержит корректное имя после получения")
        void getOne_ShouldReturnUserWithCorrectUsername_WhenUserExists() {
            User userFound = userQueryService.getOne(userExisting.getId());

            assertThat(userFound.getUsername()).isEqualTo(userExisting.getUsername());
        }

        @Test
        @DisplayName("Пользователь содержит корректную роль после получения")
        void getOne_ShouldReturnUserWithCorrectRole_WhenUserExists() {
            User userFound = userQueryService.getOne(userExisting.getId());

            assertThat(userFound.getRole()).isEqualTo(userExisting.getRole());
        }

        @Test
        @DisplayName("Пользователь содержит корректный статус активности после получения")
        void getOne_ShouldReturnUserWithCorrectEnabledStatus_WhenUserExists() {
            User userFound = userQueryService.getOne(userExisting.getId());

            assertThat(userFound.isEnabled()).isEqualTo(userExisting.isEnabled());
        }
    }

    @Nested
    @DisplayName("getMyself")
    class GetMyselfTests {

        @Test
        @DisplayName("Успешное получение текущего авторизованного пользователя")
        void getMyself_ShouldReturnCurrentUser_WhenUserAuthenticated() {
            setSecurityContext(userExisting);

            User userCurrent = userQueryService.getMyself();

            assertThat(userCurrent).isNotNull();
            assertThat(userCurrent.getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Текущий пользователь содержит корректное имя")
        void getMyself_ShouldReturnUserWithCorrectUsername_WhenUserAuthenticated() {
            setSecurityContext(userExisting);

            User userCurrent = userQueryService.getMyself();

            assertThat(userCurrent.getUsername()).isEqualTo(userExisting.getUsername());
        }

        @Test
        @DisplayName("Текущий пользователь содержит корректную роль")
        void getMyself_ShouldReturnUserWithCorrectRole_WhenUserAuthenticated() {
            setSecurityContext(userExisting);

            User userCurrent = userQueryService.getMyself();

            assertThat(userCurrent.getRole()).isEqualTo(userExisting.getRole());
        }
    }

    @Nested
    @DisplayName("findUserByUsername")
    class FindUserByUsernameTests {

        @Test
        @DisplayName("Успешный поиск пользователя по существующему имени")
        void findUserByUsername_ShouldReturnUser_WhenUsernameExists() {
            User userFound = userQueryService.findUserByUsername(userExisting.getUsername());

            assertThat(userFound).isNotNull();
            assertThat(userFound.getUsername()).isEqualTo(userExisting.getUsername());
        }

        @Test
        @DisplayName("Возврат null при поиске несуществующего имени пользователя")
        void findUserByUsername_ShouldReturnNull_WhenUsernameNotFound() {
            String usernameNonExistent = "nonExistentUser";

            User userFound = userQueryService.findUserByUsername(usernameNonExistent);

            assertThat(userFound).isNull();
        }

        @Test
        @DisplayName("Найденный пользователь содержит корректный идентификатор")
        void findUserByUsername_ShouldReturnUserWithCorrectId_WhenUsernameExists() {
            User userFound = userQueryService.findUserByUsername(userExisting.getUsername());

            assertThat(userFound.getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Найденный пользователь содержит корректную роль")
        void findUserByUsername_ShouldReturnUserWithCorrectRole_WhenUsernameExists() {
            User userFound = userQueryService.findUserByUsername(userExisting.getUsername());

            assertThat(userFound.getRole()).isEqualTo(userExisting.getRole());
        }
    }

    @Nested
    @DisplayName("getByUsername")
    class GetByUsernameTests {

        @Test
        @DisplayName("Успешное получение пользователя по существующему имени")
        void getByUsername_ShouldReturnUser_WhenUsernameExists() {
            User userFound = userQueryService.getByUsername(userExisting.getUsername());

            assertThat(userFound).isNotNull();
            assertThat(userFound.getUsername()).isEqualTo(userExisting.getUsername());
        }

        @Test
        @DisplayName("Возврат null при запросе несуществующего имени пользователя")
        void getByUsername_ShouldReturnNull_WhenUsernameNotFound() {
            String usernameNonExistent = "nonExistentUsername";

            User userFound = userQueryService.getByUsername(usernameNonExistent);

            assertThat(userFound).isNull();
        }

        @Test
        @DisplayName("Пользователь содержит корректный идентификатор после получения")
        void getByUsername_ShouldReturnUserWithCorrectId_WhenUsernameExists() {
            User userFound = userQueryService.getByUsername(userExisting.getUsername());

            assertThat(userFound.getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Пользователь содержит корректные данные после получения")
        void getByUsername_ShouldReturnUserWithCorrectData_WhenUsernameExists() {
            User userFound = userQueryService.getByUsername(userExisting.getUsername());

            assertThat(userFound.getRole()).isEqualTo(userExisting.getRole());
            assertThat(userFound.isEnabled()).isEqualTo(userExisting.isEnabled());
        }
    }
}
