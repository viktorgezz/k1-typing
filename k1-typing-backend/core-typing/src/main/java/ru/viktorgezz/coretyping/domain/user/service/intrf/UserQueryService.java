package ru.viktorgezz.coretyping.domain.user.service.intrf;

import ru.viktorgezz.coretyping.domain.user.User;

public interface UserQueryService {

    User getByUsername(String username);

    User getOne(Long id);

    User getMyself();

    User findUserByUsername(String username);
}
