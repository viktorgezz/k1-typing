package ru.viktorgezz.coretyping.auth.service;

import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.auth.dto.UserUpdatedRequest;

public interface UserDataUpdateService {

    User update(UserUpdatedRequest dto);
}
