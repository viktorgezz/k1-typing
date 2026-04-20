package ru.viktorgezz.avatarmodule.service.intrf;

import ru.viktorgezz.avatarmodule.dto.ImageDto;

public interface ProfileImgCommandService {

    void setAvatar(Long idUser, long coast, ImageDto imageDto);
}
