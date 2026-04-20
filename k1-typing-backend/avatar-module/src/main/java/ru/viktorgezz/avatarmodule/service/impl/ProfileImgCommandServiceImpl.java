package ru.viktorgezz.avatarmodule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.avatarmodule.dto.ImageDto;
import ru.viktorgezz.avatarmodule.entity.Avatar;
import ru.viktorgezz.avatarmodule.repo.AvatarRepo;
import ru.viktorgezz.avatarmodule.service.intrf.ProfileImgCommandService;
import ru.viktorgezz.coretyping.api_internal.balance.BalanceInternalService;

@Service
@RequiredArgsConstructor
public class ProfileImgCommandServiceImpl implements ProfileImgCommandService {

    private final AvatarRepo avatarRepo;
    private final BalanceInternalService balanceInternalService;

    @Override
    @Transactional
    public void setAvatar(Long idUser, long coast, ImageDto imageDto) {
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
        balanceInternalService.withdrawBalance(coast);
        avatarRepo.save(avatar);
    }
}
