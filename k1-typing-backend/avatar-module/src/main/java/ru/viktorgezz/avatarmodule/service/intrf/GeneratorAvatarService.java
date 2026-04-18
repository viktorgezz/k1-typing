package ru.viktorgezz.avatarmodule.service.intrf;

import ru.viktorgezz.avatarmodule.dto.ImageDto;

public interface GeneratorAvatarService {

    void setAvatar(Long idUser, ImageDto imageDto);
}
