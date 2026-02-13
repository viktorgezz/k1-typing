package ru.viktorgezz.avatarmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;
import ru.viktorgezz.avatarmodule.service.AvatarService;

import java.util.List;

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

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
}
