package ru.viktorgezz.coretyping.domain.user.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.dto.UserView;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends CrudRepository<User, Long> {

    User findUserByUsername(String username);

    @Query("""
            SELECT u.id as id, u.username as username
            FROM User u WHERE u.id IN :ids
            """)
    List<UserView> findUserViewByIds(@Param("ids") Iterable<Long> ids);

    @Query("SELECT u.balance FROM User u WHERE u.id = :id")
    Optional<Long> findBalanceById(Long id);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :idUser")
    long addToBalance(@Param("idUser") Long idUser, @Param("amount") Long amount);
}
