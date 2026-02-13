package ru.viktorgezz.coretyping.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.viktorgezz.coretyping.domain.user.User;

import java.util.Date;

/**
 * Сущность для хранения refresh-токенов {@link User}.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "refresh_token", nullable = false, length = 1024)
    private String token;

    @Column(name = "date_expiration", nullable = false)
    private Date dateExpiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    public RefreshToken(
            String token,
            Date dateExpiration,
            User user
    ) {
        this.token = token;
        this.dateExpiration = dateExpiration;
        this.user = user;
    }
}
