package ru.viktorgezz.k1_typing_backend.domain.balance.service;

import ru.viktorgezz.k1_typing_backend.domain.balance.dto.BalanceRsDto;
import ru.viktorgezz.k1_typing_backend.domain.result_item.Place;

public interface BalanceService {

    BalanceRsDto findBalanceByIdUser(Long idUser);

    void replenishBalanceByIdUserAndPlaceAsync(Long idUser, Place place);

    void modifyBalance(Long idUser, Long amount);

    Long withdrawBalance(Long amount);
}
