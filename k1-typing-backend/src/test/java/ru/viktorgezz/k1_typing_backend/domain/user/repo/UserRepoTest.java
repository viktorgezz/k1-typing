package ru.viktorgezz.k1_typing_backend.domain.user.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import testconfig.AbstractIntegrationPostgresTest;

@DisplayName("UserRepo Tests")
class UserRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserRepo userRepo;

    private User userExisting;

    @BeforeEach
    void setUp() {
        userExisting = userRepo.save(
                new User(
                        "testUsername",
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
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("findUserByUsername")
    class FindUserByUsernameTests {

        @Test
        @DisplayName("Возврат пользователя при поиске по существующему имени")
        void findUserByUsername_ShouldReturnUser_WhenUsernameExists() {
            User userFound = userRepo.findUserByUsername(userExisting.getUsername());

            assertThat(userFound).isNotNull();
            assertThat(userFound.getId()).isEqualTo(userExisting.getId());
        }

        @Test
        @DisplayName("Возврат null при поиске по несуществующему имени")
        void findUserByUsername_ShouldReturnNull_WhenUsernameNotExists() {
            String usernameNonExistent = "nonExistentUsername";

            User userFound = userRepo.findUserByUsername(usernameNonExistent);

            assertThat(userFound).isNull();
        }

        @Test
        @DisplayName("Найденный пользователь содержит корректные данные")
        void findUserByUsername_ShouldReturnUserWithCorrectData_WhenUsernameExists() {
            User userFound = userRepo.findUserByUsername(userExisting.getUsername());

            assertThat(userFound.getUsername()).isEqualTo("testUsername");
            assertThat(userFound.getPassword()).isEqualTo("password123");
            assertThat(userFound.getRole()).isEqualTo(Role.USER);
            assertThat(userFound.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Поиск чувствителен к регистру имени пользователя")
        void findUserByUsername_ShouldReturnNull_WhenUsernameCaseDiffers() {
            String usernameUpperCase = userExisting.getUsername().toUpperCase();

            User userFound = userRepo.findUserByUsername(usernameUpperCase);

            assertThat(userFound).isNull();
        }

        @Test
        @DisplayName("Корректный поиск среди нескольких пользователей")
        void findUserByUsername_ShouldReturnCorrectUser_WhenMultipleUsersExist() {
            User userSecond = userRepo.save(
                    new User(
                            "anotherUser",
                            "password456",
                            Role.ADMIN,
                            true,
                            false,
                            false
                    )
            );

            User userFound = userRepo.findUserByUsername("anotherUser");

            assertThat(userFound).isNotNull();
            assertThat(userFound.getId()).isEqualTo(userSecond.getId());
            assertThat(userFound.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Возврат null при поиске с пустой строкой")
        void findUserByUsername_ShouldReturnNull_WhenUsernameEmpty() {
            String usernameEmpty = "";

            User userFound = userRepo.findUserByUsername(usernameEmpty);

            assertThat(userFound).isNull();
        }
    }
}
