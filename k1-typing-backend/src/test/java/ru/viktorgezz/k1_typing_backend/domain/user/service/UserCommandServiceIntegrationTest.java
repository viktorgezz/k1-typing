package ru.viktorgezz.k1_typing_backend.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.service.intrf.UserCommandService;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("UserCommandService Integration Tests")
class UserCommandServiceIntegrationTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private UserRepo userRepo;

    private User userAuthenticated;

    @BeforeEach
    void setUp() {
        userAuthenticated = userRepo.save(
                new User(
                        "commandTestUser",
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
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Успешное сохранение нового пользователя")
        void save_ShouldPersistUser_WhenUserNew() {
            User userNew = new User(
                    "newUser",
                    "newPassword123",
                    Role.USER,
                    true,
                    false,
                    false
            );

            User userSaved = userCommandService.save(userNew);

            assertThat(userSaved).isNotNull();
            assertThat(userSaved.getId()).isNotNull();
            assertThat(userSaved.getUsername()).isEqualTo("newUser");
        }

        @Test
        @DisplayName("Пользователь сохраняется в базе данных после создания")
        void save_ShouldPersistUserInDatabase_WhenCreatedSuccessfully() {
            User userNew = new User(
                    "persistedUser",
                    "password456",
                    Role.USER,
                    true,
                    false,
                    false
            );

            User userSaved = userCommandService.save(userNew);

            Optional<User> userFound = userRepo.findById(userSaved.getId());
            assertThat(userFound).isPresent();
            assertThat(userFound.get().getUsername()).isEqualTo("persistedUser");
        }

        @Test
        @DisplayName("Успешное обновление существующего пользователя")
        void save_ShouldUpdateUser_WhenUserExists() {
            userAuthenticated.setPassword("updatedPassword");

            User userUpdated = userCommandService.save(userAuthenticated);

            assertThat(userUpdated.getPassword()).isEqualTo("updatedPassword");
        }

        @Test
        @DisplayName("Сохраненный пользователь содержит корректную роль")
        void save_ShouldSaveUserWithCorrectRole_WhenRoleProvided() {
            User userAdmin = new User(
                    "adminUser",
                    "adminPassword",
                    Role.ADMIN,
                    true,
                    false,
                    false
            );

            User userSaved = userCommandService.save(userAdmin);

            assertThat(userSaved.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Сохраненный пользователь содержит корректный статус активности")
        void save_ShouldSaveUserWithCorrectEnabledStatus_WhenStatusProvided() {
            User userDisabled = new User(
                    "disabledUser",
                    "password789",
                    Role.USER,
                    false,
                    false,
                    false
            );

            User userSaved = userCommandService.save(userDisabled);

            assertThat(userSaved.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Успешное удаление существующего пользователя")
        void delete_ShouldRemoveUser_WhenUserExists() {
            Long idUserToDelete = userAuthenticated.getId();

            User userDeleted = userCommandService.delete(idUserToDelete);

            assertThat(userDeleted).isNotNull();
            assertThat(userDeleted.getId()).isEqualTo(idUserToDelete);
            assertThat(userRepo.findById(idUserToDelete)).isEmpty();
        }

        @Test
        @DisplayName("Возврат null при попытке удалить несуществующего пользователя")
        void delete_ShouldReturnNull_WhenUserNotFound() {
            Long idUserNonExistent = 999999L;

            User userDeleted = userCommandService.delete(idUserNonExistent);

            assertThat(userDeleted).isNull();
        }

        @Test
        @DisplayName("Пользователь удаляется из базы данных после удаления")
        void delete_ShouldRemoveUserFromDatabase_WhenDeletedSuccessfully() {
            User userToDelete = userRepo.save(
                    new User(
                            "toDeleteUser",
                            "password123",
                            Role.USER,
                            true,
                            false,
                            false
                    )
            );
            Long idUserToDelete = userToDelete.getId();

            userCommandService.delete(idUserToDelete);

            Optional<User> userFound = userRepo.findById(idUserToDelete);
            assertThat(userFound).isEmpty();
        }

        @Test
        @DisplayName("Удаленный пользователь содержит корректные данные")
        void delete_ShouldReturnUserWithCorrectData_WhenUserDeleted() {
            User userToDelete = userRepo.save(
                    new User(
                            "dataCheckUser",
                            "password123",
                            Role.USER,
                            true,
                            false,
                            false
                    )
            );

            User userDeleted = userCommandService.delete(userToDelete.getId());

            assertThat(userDeleted.getUsername()).isEqualTo("dataCheckUser");
            assertThat(userDeleted.getRole()).isEqualTo(Role.USER);
        }
    }

    @Nested
    @DisplayName("deleteCurrent")
    class DeleteCurrentTests {

        @Test
        @DisplayName("Успешное удаление текущего авторизованного пользователя")
        void deleteCurrent_ShouldRemoveCurrentUser_WhenUserAuthenticated() {
            setSecurityContext(userAuthenticated);
            Long idUserCurrent = userAuthenticated.getId();

            userCommandService.deleteCurrent();

            assertThat(userRepo.findById(idUserCurrent)).isEmpty();
        }

        @Test
        @DisplayName("Текущий пользователь удаляется из базы данных")
        void deleteCurrent_ShouldRemoveUserFromDatabase_WhenDeletedSuccessfully() {
            User userCurrent = userRepo.save(
                    new User(
                            "currentUser",
                            "password123",
                            Role.USER,
                            true,
                            false,
                            false
                    )
            );
            setSecurityContext(userCurrent);
            Long idUserCurrent = userCurrent.getId();

            userCommandService.deleteCurrent();

            Optional<User> userFound = userRepo.findById(idUserCurrent);
            assertThat(userFound).isEmpty();
        }
    }
}
