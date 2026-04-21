package ru.viktorgezz.coretyping.domain.user.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.dto.UserView;
import testconfig.AbstractRepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("UserRepo Tests")
@DataJpaTest
class UserRepoTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestEntityManager entityManager;

    private User userPersisted;

    @BeforeEach
    void setUp() {
        final String password = "1234567654";
        final String name = "test1";
        User user = new User(
                name,
                password,
                Role.USER,
                true,
                false,
                false
        );
        user.setBalance(100L);
        userPersisted = entityManager.persistAndFlush(user);
        entityManager.clear(); // Очищаем кэш, чтобы тесты шли честно в БД
    }

    @Test
    @DisplayName("Поиск по существующему имени пользователя")
    void findUserByUsername_ExistingUsername_ReturnsUser() {
        // Arrange
        String usernameExisting = "test1";

        // Act
        User userFound = userRepo.findUserByUsername(usernameExisting);

        // Assert (Self-validating)
        assertThat(userFound).isNotNull();
        assertThat(userFound.getUsername()).isEqualTo(usernameExisting);
    }

    @Test
    @DisplayName("Поиск проекций по списку идентификаторов")
    void findUserViewByIds_ValidIds_ReturnsUserViewList() {
        // Arrange
        List<Long> idsToFind = List.of(userPersisted.getId());

        // Act
        List<UserView> viewsFound = userRepo.findUserViewByIds(idsToFind);

        // Assert
        UserView viewFirst = viewsFound.getFirst();
        assertThat(viewFirst.getId()).isEqualTo(userPersisted.getId());
        assertThat(viewFirst.getUsername()).isEqualTo("test1");
    }

    @Test
    @DisplayName("Получение баланса по существующему ID")
    void findBalanceById_ExistingId_ReturnsBalanceOptional() {
        // Arrange
        Long idExisting = userPersisted.getId();

        // Act
        Optional<Long> balanceOptional = userRepo.findBalanceById(idExisting);

        // Assert
        assertThat(balanceOptional).isPresent();
        assertThat(balanceOptional).contains(100L);
    }

    @Test
    @DisplayName("Увеличение баланса пользователя")
    void addToBalance_PositiveAmount_UpdatesBalanceAndReturnsCount() {
        // Arrange
        Long idTarget = userPersisted.getId();
        Long amountToAdd = 50L;

        // Act
        long rowsUpdated = userRepo.addToBalance(idTarget, amountToAdd);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Long> balanceUpdated = userRepo.findBalanceById(idTarget);
        assertThat(rowsUpdated).isEqualTo(1);
        assertThat(balanceUpdated).isPresent();
        assertThat(balanceUpdated).contains(150L);
    }

    @Test
    @DisplayName("Поиск по несуществующему имени")
    void findUserByUsername_NonExistentUsername_ReturnsNull() {
        // Arrange
        String usernameMissing = "unknown_user";

        // Act
        User userNull = userRepo.findUserByUsername(usernameMissing);

        // Assert
        assertThat(userNull).isNull();
    }
}