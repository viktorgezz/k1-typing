package ru.viktorgezz.security.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.security.model.RefreshToken;

import java.util.Date;
import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link RefreshToken}.
 */
public interface RefreshTokenRepo extends CrudRepository<RefreshToken, Long> {

    @Query("SELECT rt.token FROM RefreshToken rt WHERE rt.username = :username")
    List<String> findValueRefreshTokenByUsername(@Param("username") String username);

    @Modifying
    void deleteByToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.dateExpiration < :dateNow")
    void deleteExpiredTokens(@Param("dateNow") Date dateNow);

    @Modifying
    @Query("""
            DELETE FROM RefreshToken rt
            WHERE rt.dateExpiration < :dateNow
            AND rt.username = :username
            """)
    void deleteExpiredTokensByUsername(@Param("dateNow") Date dateNow, @Param("username") String username);

}
