package ru.viktorgezz.coretyping.domain.participant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.user.User;

@Entity
@Table(
        name = "participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_contest", "id_user"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Participants {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contest")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    public Participants(Contest contest, User user) {
        this.contest = contest;
        this.user = user;
    }
}

