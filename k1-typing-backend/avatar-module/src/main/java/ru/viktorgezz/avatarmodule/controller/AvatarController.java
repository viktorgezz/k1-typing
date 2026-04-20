package ru.viktorgezz.avatarmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;
import ru.viktorgezz.avatarmodule.service.intrf.ProfileImgQueryService;
import ru.viktorgezz.avatarmodule.service.intrf.UploadingProfilePicture;

import java.util.List;

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final ProfileImgQueryService profileImgQueryService;
    private final UploadingProfilePicture uploadingProfilePicture;

    @GetMapping("/me")
    public AvatarProfileRsDto showMyAvatar() {
        return profileImgQueryService.getCurrentUserAvatar();
    }

    @GetMapping
    public List<ParticipantAvatarRsDto> showAvatarsParticipant(
            @RequestParam List<Long> idsUser
    ) {
        return profileImgQueryService.getAvatarsByUserIds(idsUser);
    }

    @PostMapping
    public void uploadCustomAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        uploadingProfilePicture.uploadCustomImg(file);
    }

    @PostMapping("/generated")
    public void uploadGeneratedAvatar(
            @RequestParam("promt") String promt
    ) {
        uploadingProfilePicture.uploadGeneratedImg(promt);
    }

}
