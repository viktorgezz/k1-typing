package ru.viktorgezz.coretyping.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;
import ru.viktorgezz.coretyping.properties.CustomProperties;
import ru.viktorgezz.coretyping.security.service.JwtService;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Конфигурация безопасности Spring Security для приложения.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/ui/auth/**",
            "/monitoring/**"
    };

    /**
     * WebSocket endpoints - аутентификация происходит на уровне STOMP,
     * поэтому HTTP handshake разрешаем всем.
     */
    private static final String[] WEBSOCKET_URLS = {
            "/ws/**",
            "/ws/contest/**",
            "/ws/contest"
    };

    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final CustomProperties customProperties;

    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    )
            throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(PUBLIC_URLS)
                                .permitAll()
                                .requestMatchers(WEBSOCKET_URLS)
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/exercise", "/exercise/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/result_item/single-contest")
                                .permitAll()
                                .anyRequest()
                                .hasAnyRole(Arrays.stream(Role.values()).map(Role::name).toArray(String[]::new))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final String urlFrontend = format("%s:%s", customProperties.getHostFrontend(), customProperties.getPortFrontend());
        log.debug("[allowedOrigin: {}]", urlFrontend);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(urlFrontend, customProperties.getHostFrontend(), "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PATCH", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User userFound = userRepo.findUserByUsername(username);
            if (userFound == null) {
                throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
            }
            return userFound;
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            UserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

}
