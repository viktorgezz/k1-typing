package ru.viktorgezz.statistics_result_module.result_item;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Column(name = "id_contest")
    private Long idContest;

    @Column(name = "id_user")
    private Long idUser;

    public ResultItem(
            Long durationSeconds,
            Integer speed,
            BigDecimal accuracy,
            Place place,
            Long idContest,
            Long idUser
    ) {
        this.durationSeconds = durationSeconds;
        this.speed = speed;
        this.accuracy = accuracy;
        this.place = place;
        this.idContest = idContest;
        this.idUser = idUser;
    }
}
