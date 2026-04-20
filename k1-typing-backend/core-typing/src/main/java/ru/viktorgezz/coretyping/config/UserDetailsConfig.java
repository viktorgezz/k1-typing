package ru.viktorgezz.coretyping.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UserRepo userRepo;

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
}
