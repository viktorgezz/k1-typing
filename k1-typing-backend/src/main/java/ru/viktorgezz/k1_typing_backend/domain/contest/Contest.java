package ru.viktorgezz.k1_typing_backend.domain.contest;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.participant.Participants;
import ru.viktorgezz.k1_typing_backend.domain.result_item.ResultItem;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contests")
@Getter
@Setter
@NoArgsConstructor
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Positive
    @Column(nullable = false)
    private Integer amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_exercises", nullable = false)
    private Exercise exercise;

    @OneToMany(
            mappedBy = "contest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Participants> participants;

    @OneToMany(
            mappedBy = "contest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ResultItem> resultItems;

    public Contest(Status status, Integer amount, Exercise exercise) {
        this.status = status;
        this.amount = amount;
        this.exercise = exercise;
    }
}
