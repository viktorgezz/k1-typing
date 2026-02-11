package ru.viktorgezz.k1_typing_backend.domain.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.k1_typing_backend.domain.balance.dto.BalanceRsDto;
import ru.viktorgezz.k1_typing_backend.domain.balance.service.BalanceService;
import ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/balance")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping
    public BalanceRsDto viewOwnBalance() {
        return balanceService.findBalanceByIdUser(
                CurrentUserUtils.getCurrentUser().getId()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void modifyBalance(
            @RequestParam Long idUser,
            @RequestParam Long amount
    ) {
        balanceService.modifyBalance(idUser, amount);
    }

}
