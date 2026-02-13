package ru.viktorgezz.coretyping.domain.user.service.intrf;

import ru.viktorgezz.coretyping.domain.user.User;

public interface UserCommandService {

    User save(User user);

    User delete(Long id);

    /**
     * Удаляет текущего аутентифицированного пользователя.
     */
    void deleteCurrent();

}
