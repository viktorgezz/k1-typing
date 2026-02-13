package ru.viktorgezz.avatarmodule.service;

import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;

import java.util.List;

public interface AvatarService {

    AvatarProfileRsDto getCurrentUserAvatar();

    List<ParticipantAvatarRsDto> getAvatarsByUserIds(List<Long> idsUser);

}
