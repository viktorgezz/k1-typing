package ru.viktorgezz.coretyping.domain.result_item;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.user.User;

@Entity
@Table(name = "result_items")
@Getter
@Setter
@NoArgsConstructor
public class ResultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(nullable = false)
    private Integer speed;

    @Column(precision = 5, scale = 2, nullable = false)
    @Positive
    private BigDecimal accuracy;

    @Column
    @Enumerated(EnumType.STRING)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contest")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    public ResultItem(Long durationSeconds, Integer speed, BigDecimal accuracy, Place place, Contest contest, User user) {
        this.durationSeconds = durationSeconds;
        this.speed = speed;
        this.accuracy = accuracy;
        this.place = place;
        this.contest = contest;
        this.user = user;
    }
}
