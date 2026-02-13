package ru.viktorgezz.avatarmodule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avatar")
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
}
