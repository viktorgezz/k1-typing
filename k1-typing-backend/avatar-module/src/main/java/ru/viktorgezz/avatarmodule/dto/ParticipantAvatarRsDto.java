package ru.viktorgezz.avatarmodule.dto;

public record ParticipantAvatarRsDto(
        Long idUser,
        byte[] photo,
        String contentType
) {
}
