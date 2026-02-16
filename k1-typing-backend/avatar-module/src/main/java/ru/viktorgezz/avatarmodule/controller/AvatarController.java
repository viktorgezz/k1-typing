package ru.viktorgezz.avatarmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.avatarmodule.dto.AvatarProfileRsDto;
import ru.viktorgezz.avatarmodule.dto.ParticipantAvatarRsDto;
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

    @PostMapping
    public void createNewAvatar(
            @RequestParam String promt
    ) {
        final Long COAST_GENERATION = 2L;
        balanceInternalService.withdrawBalance(COAST_GENERATION);
        generatorAvatarService.generateAvatarAsync(promt, getCurrentIdUser());
    }
}
