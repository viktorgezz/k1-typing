package ru.viktorgezz.avatarmodule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.avatarmodule.dto.ImageDto;
import ru.viktorgezz.avatarmodule.entity.Avatar;
import ru.viktorgezz.avatarmodule.repo.AvatarRepo;
import ru.viktorgezz.avatarmodule.service.intrf.GeneratorAvatarService;

@Service
@RequiredArgsConstructor
public class GeneratorAvatarServiceImpl implements GeneratorAvatarService {

    private final AvatarRepo avatarRepo;

    @Override
    @Transactional
    public void setAvatar(Long idUser, ImageDto imageDto) {
        Avatar avatar = avatarRepo.findByIdUser(idUser)
                .map(avatarExisting -> {
                    avatarExisting.setPhoto(imageDto.data());
                    avatarExisting.setContentType(imageDto.contentType());
                    return avatarExisting;
                })
                .orElseGet(() -> new Avatar(
                        idUser,
                        imageDto.data(),
                        imageDto.contentType()
                ));

        avatarRepo.save(avatar);
    }
}
