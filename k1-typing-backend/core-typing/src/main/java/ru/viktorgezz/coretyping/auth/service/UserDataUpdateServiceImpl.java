package ru.viktorgezz.coretyping.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.auth.dto.UserUpdatedRequest;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserCommandService;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserQueryService;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserDataUpdateServiceImpl implements UserDataUpdateService {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User update(UserUpdatedRequest userDto) {
        User userCurrent = getCurrentUser();

        if (isUsernameTakenByAnotherUser(userDto.username(), userCurrent.getId())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS, userDto.username());
        }
        userCurrent.setUsername(userDto.username());
        userCurrent.setPassword(passwordEncoder.encode(userDto.newPassword()));

        return userCommandService.save(userCurrent);
    }

    /**
     * Проверяет, занят ли username другим пользователем.
     *
     * @param username      имя пользователя для проверки
     * @param currentUserId ID текущего пользователя
     * @return true если username занят другим пользователем, false в противном случае
     */
    private boolean isUsernameTakenByAnotherUser(String username, Long currentUserId) {
        User userByUsername = userQueryService.findUserByUsername(username);
        return userByUsername != null && !currentUserId.equals(userByUsername.getId());
    }
}
