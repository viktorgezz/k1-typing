package ru.viktorgezz.k1_typing_backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.AuthenticationResponse;
import ru.viktorgezz.k1_typing_backend.auth.dto.LogoutRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.RefreshRequest;
import ru.viktorgezz.k1_typing_backend.auth.dto.RegistrationRequest;
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
    public void logout(@RequestBody @Valid final LogoutRequest refreshTokenDto){
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
