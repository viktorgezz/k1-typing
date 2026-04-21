package ru.viktorgezz.coretyping.domain.user.service.intrf;

import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.dto.UserView;

import java.util.List;

public interface UserQueryService {

    User getByUsername(String username);

    User getOne(Long id);

    User getMyself();

    List<UserView> findUserViewByIds(Iterable<Long> idsUser);

    User findUserByUsername(String username);
}
