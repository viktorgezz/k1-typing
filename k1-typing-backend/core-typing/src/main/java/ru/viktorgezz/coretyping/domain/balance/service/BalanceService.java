package ru.viktorgezz.coretyping.domain.balance.service;

import ru.viktorgezz.coretyping.domain.balance.dto.BalanceRsDto;
import ru.viktorgezz.statistics_result_module.result_item.Place;

public interface BalanceService {

    BalanceRsDto findBalanceByIdUser(Long idUser);

    void replenishBalanceByIdUserAndPlaceAsync(Long idUser, Place place);

    void modifyBalance(Long idUser, Long amount);

    Long withdrawBalance(Long amount);
}
