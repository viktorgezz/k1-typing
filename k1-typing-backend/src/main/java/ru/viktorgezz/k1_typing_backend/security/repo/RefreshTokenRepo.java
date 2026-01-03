package ru.viktorgezz.k1_typing_backend.security.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.k1_typing_backend.security.model.RefreshToken;

import java.util.Date;
import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link RefreshToken}.
 */
public interface RefreshTokenRepo extends CrudRepository<RefreshToken, Long> {

    @Modifying
    void deleteByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.username = :username")
    List<RefreshToken> findRefreshTokensByUsername(String username);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :id_user")
    List<RefreshToken> findRefreshTokensByIdUser(@Param("id_user") Long idUser);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.dateExpiration < :dateNow")
    void deleteExpiredTokens(@Param("dateNow") Date dateNow);

}
