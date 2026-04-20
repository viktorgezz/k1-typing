package ru.viktorgezz.coretyping.util;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import ru.viktorgezz.coretyping.domain.user.User;

/**
 * Утилитный класс для получения текущего аутентифицированного пользователя из SecurityContext.
 */
public class CurrentUserUtils {

    private CurrentUserUtils() {
    }

    /**
     * Получает текущего аутентифицированного пользователя из SecurityContext.
     *
     * @return объект {@link User} текущего пользователя
     * @throws RuntimeException если пользователь не аутентифицирован или principal не является User
     */
    public static User getCurrentUser() {
        final UserDetails userDetails =
                ru.viktorgezz.security.util.CurrentUserUtils.getCurrentUser();

        if (!(userDetails instanceof User)) {
            throw new AuthenticationServiceException("Authentication userDetails is not an instance of User");
        }

        return (User) userDetails;
    }
}
