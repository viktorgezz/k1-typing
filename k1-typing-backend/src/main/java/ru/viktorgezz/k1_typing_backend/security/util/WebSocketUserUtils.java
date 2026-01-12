package ru.viktorgezz.k1_typing_backend.security.util;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

import java.security.Principal;

public class WebSocketUserUtils {

    private WebSocketUserUtils() {}

    public static User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new AuthenticationServiceException("User is not authenticated");
        }

        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object authPrincipal = auth.getPrincipal();
            if (authPrincipal instanceof User user) {
                return user;
            }
        }

        throw new AuthenticationServiceException("Cannot extract user from principal");
    }
}
