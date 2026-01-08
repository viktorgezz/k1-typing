package ru.viktorgezz.k1_typing_backend.domain.exercises;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "exercise",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<Contest> contests;

    public Exercise(String title, String text, User user) {
        this.title = title;
        this.text = text;
        this.user = user;
    }

    public Exercise(String title, String text, User user, Language language) {
        this.title = title;
        this.text = text;
        this.user = user;
        this.language = language;
    }
}
