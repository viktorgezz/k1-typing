package ru.viktorgezz.coretyping.security;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.viktorgezz.coretyping.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.coretyping.security.exception.TokenExpiredException;
import ru.viktorgezz.coretyping.security.service.JwtService;

import java.io.IOException;

/**
 * Фильтр для аутентификации по JWT.
 * <p>
 * Извлечения токена:
 * Заголовок {@code Authorization: Bearer <token>}
 * <p>
 * При успешной валидации формирует {@link UsernamePasswordAuthenticationToken}
 * и записывает его в {@link SecurityContextHolder}.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String tokenAccess = getAccessToken(request);

            if (tokenAccess == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.extractUsername(tokenAccess);

            if (username != null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(tokenAccess, userDetails.getUsername())) {
                    final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (TokenExpiredException | InvalidJwtTokenException e) {
            log.debug("{}", e.getMessage());
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
