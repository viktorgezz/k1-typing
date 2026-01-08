package ru.viktorgezz.k1_typing_backend.auth.service;

import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.auth.dto.UserUpdatedRequest;

public interface UserDataUpdateService {

    User update(UserUpdatedRequest dto);
}
