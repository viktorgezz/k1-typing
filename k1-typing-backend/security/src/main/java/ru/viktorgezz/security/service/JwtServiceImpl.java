package ru.viktorgezz.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.security.UserDetailsCustom;
import ru.viktorgezz.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.security.exception.TokenExpiredException;
import ru.viktorgezz.security.model.RefreshToken;
import ru.viktorgezz.security.properties.JwtProperties;
import ru.viktorgezz.security.util.KeyUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

/**
 * Сервис работы с JWT-токенами. Реализует {@link JwtService}.
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS_TYPE = "ACCESS_TOKEN";
    private static final String ROLE = "role";
    private static final String ID = "id_user";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public JwtServiceImpl(
            JwtProperties jwtProperties,
            UserDetailsService userDetailsService,
            RefreshTokenService refreshTokenService
    ) throws Exception {
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.privateKey = KeyUtils.loadPrivateKey("keys/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/public_key.pem");
        this.accessTokenExpiration = jwtProperties.getAccessExpirationMs();
        this.refreshTokenExpiration = jwtProperties.getRefreshExpirationMs();
    }

    @Override
    public String generateAccessToken(final UserDetailsCustom userDetailsCustom) {
        final String role = userDetailsCustom.getRoleWithoutPrefix();
        final String username = userDetailsCustom.getUsername();
        final Long id = userDetailsCustom.getId();

        log.debug("Generate acc-token for {} with role: {}", username, role);
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, ACCESS_TYPE,
                ROLE, role,
                ID, id
        );
        return buildToken(username, claims, accessTokenExpiration);
    }

    private String generateAccessToken(
            final String username,
            final String role,
            final Long id
    ) {
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, ACCESS_TYPE,
                ROLE, role,
                ID, id
        );

        return buildToken(username, claims, accessTokenExpiration);
    }

    @Transactional
    @Override
    public String generateRefreshToken(final UserDetails userDetails) {
        final String username = userDetails.getUsername();
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        final String refreshToken = buildToken(username, claims, refreshTokenExpiration);

        RefreshToken token = new RefreshToken(
                username,
                refreshToken,
                new Date(System.currentTimeMillis() + refreshTokenExpiration)
        );

        refreshTokenService.deleteExpiredUserRefreshToken(username);
        refreshTokenService.save(token);
        return refreshToken;
    }

    @Override
    public boolean validateToken(
            final String token,
            final String usernameExpected,
            final String usernameExtract
    ) {
        Claims claims = extractClaimsStrict(token);
        String type = claims.get(TOKEN_TYPE, String.class);
        return type.equals(ACCESS_TYPE)
                && usernameExtract.equals(usernameExpected)
                && !isTokenExpired(token);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaimsStrict(token).getSubject();
    }

    @Override
    public String refreshToken(final String refreshToken) {
        final Claims claims = extractClaimsAllowExpired(refreshToken);
        final String username = claims.getSubject();
        final UserDetailsCustom userDetailsCustom = (UserDetailsCustom) userDetailsService.loadUserByUsername(username);
        final String role = userDetailsCustom.getRoleWithoutPrefix();
        final Long id = userDetailsCustom.getId();

        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new InvalidJwtTokenException("Invalid refresh token");
        } else if (isExpired(claims) || !isRefreshTokenExists(refreshToken, username)) {
            throw new TokenExpiredException("Refresh token expired");
        }

        return generateAccessToken(username, role, id);
    }

    @Transactional
    @Override
    public void dropRefreshToken(final String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    private String buildToken(
            String username,
            Map<String, Object> claims,
            long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return isExpired(extractClaimsStrict(token));
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date(System.currentTimeMillis()));
    }

    private Claims extractClaimsAllowExpired(String token) {
        try {
            return extractClaims(token);
        } catch (final ExpiredJwtException e) {
            return e.getClaims();
        } catch (final JwtException e) {
            throw new InvalidJwtTokenException(e.getMessage());
        }
    }

    private Claims extractClaimsStrict(String token) {
        try {
            return extractClaims(token);
        } catch (final ExpiredJwtException e) {
            throw new TokenExpiredException("Invalid access token");
        } catch (final JwtException e) {
            throw new InvalidJwtTokenException(e.getMessage());
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isRefreshTokenExists(String refreshToken, String username) {
        if (refreshToken == null) {
            return true;
        }

        return refreshTokenService
                .findValuesRefreshTokensByUsername(username)
                .contains(refreshToken);

    }
}
