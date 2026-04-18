package ru.viktorgezz.avatarmodule.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ImageDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;
import ru.viktorgezz.avatarmodule.exception.BusinessException;
import ru.viktorgezz.avatarmodule.exception.ErrorCode;
import ru.viktorgezz.avatarmodule.service.intrf.AvatarService;
import ru.viktorgezz.avatarmodule.service.intrf.GeneratorAvatarService;
import ru.viktorgezz.coretyping.api_internal.balance.BalanceInternalService;

import java.util.List;

import static ru.viktorgezz.avatarmodule.util.CurrentUserUtils.getCurrentIdUser;

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final GeneratorAvatarService generatorAvatarService;
    private final BalanceInternalService balanceInternalService;

    @GetMapping("/me")
    public AvatarProfileRsDto showMyAvatar() {
        return avatarService.getCurrentUserAvatar();
    }

    @GetMapping
    public List<ParticipantAvatarRsDto> showAvatarsParticipant(
            @RequestParam List<Long> idsUser
    ) {
        return avatarService.getAvatarsByUserIds(idsUser);
    }

    @SneakyThrows
    @PostMapping
    public void setNewAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        String contentType = file.getContentType();
        if (contentType == null || !isSupportedType(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }
        final Long COAST_GENERATION = 2L;
        balanceInternalService.withdrawBalance(COAST_GENERATION);
        ImageDto imageDto = new ImageDto(
                file.getBytes(),
                contentType
        );
        generatorAvatarService.setAvatar(getCurrentIdUser(), imageDto);
    }

    private static boolean isSupportedType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif");
    }
}
