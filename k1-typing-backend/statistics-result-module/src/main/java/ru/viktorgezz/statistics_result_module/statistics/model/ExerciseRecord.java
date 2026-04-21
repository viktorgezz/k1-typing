package ru.viktorgezz.statistics_result_module.statistics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "exercise_records_view")
@NoArgsConstructor
@Getter
public class ExerciseRecord {

    @Id
    @Column(name = "id_exercise")
    private Long idExercise;

    @Column(name = "title")
    private String title;

    @Column(name = "username")
    private String username;

    @Column(name = "max_speed")
    private Integer maxSpeed;

    @Column(name = "min_duration")
    private Long minDuration;
}
