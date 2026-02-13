package ru.viktorgezz.coretyping.domain.balance.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.balance.dto.BalanceRsDto;
import ru.viktorgezz.coretyping.domain.result_item.Place;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public BalanceRsDto findBalanceByIdUser(Long idUser) {
        return new BalanceRsDto(
                userRepo.findBalanceById(idUser)
                        .orElseThrow(EntityExistsException::new)
        );
    }

    @Override
    @Async
    @Transactional
    public void replenishBalanceByIdUserAndPlaceAsync(
            Long idUser,
            Place place) {
        final Long reward = getRewardByPlace(place);
        if (reward == null) {
            return;
        }

        userRepo.addToBalance(idUser, reward);
    }

    @Override
    @Transactional
    public void modifyBalance(Long idUser, Long amount) {
        userRepo.addToBalance(idUser, amount);
    }

    @Override
    @Transactional
    public Long withdrawBalance(Long amount) {
        amount = Math.abs(amount) * (-1);
        return userRepo.addToBalance(getCurrentUser().getId(), amount);
    }

    private static Long getRewardByPlace(Place place) {
        return switch (place) {
            case FIRST -> 3L;
            case SECOND -> 2L;
            case THIRD -> 1L;
            default -> null;
        };
    }
}
