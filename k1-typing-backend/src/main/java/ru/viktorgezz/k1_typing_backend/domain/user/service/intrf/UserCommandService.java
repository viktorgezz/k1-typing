package ru.viktorgezz.k1_typing_backend.domain.user.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.user.User;

public interface UserCommandService {

    User save(User user);

    User delete(Long id);

    /**
     * Удаляет текущего аутентифицированного пользователя.
     */
    void deleteCurrent();

}
