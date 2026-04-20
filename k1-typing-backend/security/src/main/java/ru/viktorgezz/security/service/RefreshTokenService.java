package ru.viktorgezz.security.service;

import ru.viktorgezz.security.model.RefreshToken;

import java.util.List;

public interface RefreshTokenService {

    RefreshToken save(RefreshToken refreshToken);

    void deleteExpiredUserRefreshToken(String username);

    void deleteByToken(String refreshToken);

    List<String> findValuesRefreshTokensByUsername(String username);
}
