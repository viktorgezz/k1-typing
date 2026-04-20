package ru.viktorgezz.avatarmodule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avatars")
@Getter
@Setter
@NoArgsConstructor
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "id_user", unique = true, nullable = false)
    private Long idUser;

    @Column(name = "photo", nullable = false)
    private byte[] photo;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    public Avatar(Long idUser, byte[] photo, String contentType) {
        this.idUser = idUser;
        this.photo = photo;
        this.contentType = contentType;
    }
}
