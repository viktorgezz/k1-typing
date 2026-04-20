package ru.viktorgezz.coretyping.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.auth.dto.AuthenticationRequest;
import ru.viktorgezz.coretyping.auth.dto.AuthenticationResponse;
import ru.viktorgezz.coretyping.auth.dto.RefreshRequest;
import ru.viktorgezz.coretyping.auth.dto.RegistrationRequest;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserCommandService;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;
import ru.viktorgezz.security.service.JwtService;
import ru.viktorgezz.security.service.RefreshTokenService;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static ru.viktorgezz.coretyping.util.CurrentUserUtils.getCurrentUser;

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
    private final RefreshTokenService refreshTokenService;

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
        final String accessToken = jwtService.generateAccessToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);
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
        } catch (Exception e) {
            log.debug("Refresh token expired/invalid: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_REFRESH_EXPIRED);
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            final String username = getCurrentUser().getUsername();
            final List<String> refreshTokens = refreshTokenService
                    .findValuesRefreshTokensByUsername(username);

            log.debug("current refreshToken: {}, username: {}, refreshTokens: {}",
                    refreshToken, username, refreshTokens);

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
