package ru.viktorgezz.avatarmodule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.avatarmodule.client.ImageGeneration;
import ru.viktorgezz.avatarmodule.dto.GeneratedImageDto;
import ru.viktorgezz.avatarmodule.entity.Avatar;
import ru.viktorgezz.avatarmodule.repo.AvatarRepo;
import ru.viktorgezz.avatarmodule.service.intrf.GeneratorAvatarService;

import static ru.viktorgezz.avatarmodule.util.CurrentUserUtils.getCurrentIdUser;

@Service
@RequiredArgsConstructor
public class GeneratorAvatarServiceImpl implements GeneratorAvatarService {

    private final ImageGeneration imageGeneration;
    private final AvatarRepo avatarRepo;

    @Override
    @Async
    @Transactional
    public void generateAvatarAsync(String promt, Long idUser) {
        GeneratedImageDto imageGenerated = imageGeneration.generateImage(idUser, promt);

        Avatar avatar = avatarRepo.findByIdUser(idUser)
                .map(avatarExisting -> {
                    avatarExisting.setPhoto(imageGenerated.data());
                    avatarExisting.setContentType(imageGenerated.contentType());
                    return avatarExisting;
                })
                .orElseGet(() -> new Avatar(
                        idUser,
                        imageGenerated.data(),
                        imageGenerated.contentType()
                ));

        avatarRepo.save(avatar);
    }
}
