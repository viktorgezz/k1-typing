package ru.viktorgezz.statistics_result_module.statistics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "user_leaderboard_view")
@NoArgsConstructor
@Getter
public class UserLeaderboard {

    @Id
    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "rank_place")
    private Long rankPlace;

    @Column(name = "username")
    private String username;

    @Column(name = "average_speed", precision = 10, scale = 2)
    private BigDecimal averageSpeed;

    @Column(name = "count_contest")
    private Long countContest;

    @Column(name = "first_places_count")
    private Long firstPlacesCount;
}
