package ru.viktorgezz.k1_typing_backend.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.service.intrf.UserQueryService;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepo userRepo;

    @Override
    public User getByUsername(String username) {
        return userRepo.findUserByUsername(username);
    }
}
