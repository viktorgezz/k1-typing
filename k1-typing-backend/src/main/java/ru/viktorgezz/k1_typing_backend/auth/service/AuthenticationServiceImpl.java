package ru.viktorgezz.k1_typing_backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationResponse;
import ru.viktorgezz.k1_typing_backend.auth.dto.RefreshRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.RegistrationRequest;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.service.intrf.UserCommandService;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;
import ru.viktorgezz.k1_typing_backend.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.k1_typing_backend.security.exception.TokenExpiredException;
import ru.viktorgezz.k1_typing_backend.security.model.RefreshToken;
import ru.viktorgezz.k1_typing_backend.security.repo.RefreshTokenRepo;
import ru.viktorgezz.k1_typing_backend.security.service.JwtService;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils.getCurrentUser;

/**
 * Сервис аутентификации пользователей. Реализует {@link AuthenticationService}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserCommandService userCommandService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    public AuthenticationResponse login(AuthenticationRequest authRq) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRq.name(),
                            authRq.password()
                    )
            );
        } catch (InternalAuthenticationServiceException e) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }

        final User user = (User) authentication.getPrincipal();
        final String accessToken = jwtService.generateAccessToken(user.getUsername());
        final String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        final String tokenType = "Bearer";

        log.debug("acces token: {}", accessToken);

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                tokenType
        );
    }

    @Override
    public void register(RegistrationRequest request, Role role) {
        checkPasswords(request.password(), request.confirmPassword());
        final User user = new User(
                request.username(),
                request.password(),
                role,
                true,
                false,
                false
        );
        user.setPassword(passwordEncoder.encode(request.password()));
        userCommandService.save(user);
        log.debug("Registering user: {}", user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        try {
            final String accessNewToken = jwtService.refreshToken(request.refreshToken());
            final String tokenType = "Bearer";

            return new AuthenticationResponse(
                    accessNewToken,
                    request.refreshToken(),
                    tokenType
            );
        } catch (InvalidJwtTokenException | TokenExpiredException e) {
            log.debug("Refresh token expired/invalid: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_REFRESH_EXPIRED);
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            final Long idUser = getCurrentUser().getId();
            final List<String> refreshTokens = refreshTokenRepo.findRefreshTokensByIdUser(idUser)
                    .stream()
                    .map(RefreshToken::getToken)
                    .toList();

            log.debug("current refreshToken: {}, id user: {}, refreshTokens: {}",
                    refreshToken, idUser, refreshTokens);

            if (!refreshTokens.contains(refreshToken)) {
                throw new AccessDeniedException("Token does not belong to current user");
            }

            jwtService.dropRefreshToken(refreshToken);
        } catch (Exception e) {
            log.debug("Invalid refresh token {}", e.getMessage());
        }
    }

    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }
}
