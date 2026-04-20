package ru.viktorgezz.security.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.security.model.RefreshToken;
import ru.viktorgezz.security.repo.RefreshTokenRepo;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    @Transactional
    public void deleteExpiredUserRefreshToken(String username) {
        Date now = new Date();
        refreshTokenRepo.deleteExpiredTokensByUsername(now, username);
    }

    @Override
    @Transactional
    public void deleteByToken(String refreshToken) {
        refreshTokenRepo.deleteByToken(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findValuesRefreshTokensByUsername(String username) {
        final List<String> token = refreshTokenRepo.findValueRefreshTokenByUsername(username);
        if (token == null) {
            throw new EntityExistsException();
        }
        return token;
    }
}
