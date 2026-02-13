package ru.viktorgezz.avatarmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;
import ru.viktorgezz.avatarmodule.entity.Avatar;
import ru.viktorgezz.avatarmodule.exception.BusinessException;
import ru.viktorgezz.avatarmodule.exception.ErrorCode;
import ru.viktorgezz.avatarmodule.repo.AvatarRepo;

import java.util.List;
import java.util.stream.StreamSupport;

import static ru.viktorgezz.avatarmodule.util.CurrentUserUtils.getCurrentIdUser;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepo avatarRepo;

    @Override
    public AvatarProfileRsDto getCurrentUserAvatar() {
        Avatar avatarFound = avatarRepo
                .findById(getCurrentIdUser())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.AVATAR_NOT_FOUND, getCurrentIdUser().toString())
                );

        return new AvatarProfileRsDto(
                avatarFound.getPhoto(),
                avatarFound.getContentType()
        );
    }

    @Override
    public List<ParticipantAvatarRsDto> getAvatarsByUserIds(List<Long> idsUser) {
        return StreamSupport.stream(
                avatarRepo.findAllById(idsUser).spliterator(), false)
                .map(avatar -> new ParticipantAvatarRsDto(
                        avatar.getIdUser(),
                        avatar.getPhoto(),
                        avatar.getContentType()
                )).toList();
    }
}
