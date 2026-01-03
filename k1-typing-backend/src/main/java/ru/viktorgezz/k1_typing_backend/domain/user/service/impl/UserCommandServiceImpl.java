package ru.viktorgezz.k1_typing_backend.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.domain.user.repo.UserRepo;
import ru.viktorgezz.k1_typing_backend.domain.user.service.intrf.UserCommandService;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepo userRepo;

    @Transactional
    @Override
    public User save(User user) {
        return userRepo.save(user);
    }
}
