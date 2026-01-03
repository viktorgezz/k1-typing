package ru.viktorgezz.k1_typing_backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.k1_typing_backend.auth.dto.*;
import ru.viktorgezz.k1_typing_backend.auth.service.AuthenticationService;
import ru.viktorgezz.k1_typing_backend.domain.user.Role;

/**
 * REST-контроллер для операций аутентификации пользователей.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthenticationResponse login(
            @RequestBody
            @Valid
            final AuthenticationRequest authenticationRequest
    ){
        return authenticationService.login(authenticationRequest);
    }

    @PostMapping("/logout")
    public void logout(final LogoutRequest refreshTokenDto){
        authenticationService.logout(refreshTokenDto.refreshToken());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(
            @RequestBody
            @Valid
            final RegistrationRequest request
    ){
        authenticationService.register(request, Role.USER);
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(
            @RequestBody
            @Valid
            final RefreshRequest request
    ){
        return authenticationService.refreshToken(request);
    }
}
