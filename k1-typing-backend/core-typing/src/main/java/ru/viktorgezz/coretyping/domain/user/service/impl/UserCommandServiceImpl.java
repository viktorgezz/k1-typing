package ru.viktorgezz.coretyping.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserCommandService;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepo userRepo;

    @Override
    @Transactional
    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public User delete(Long id) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            userRepo.delete(user);
        }
        return user;
    }

    @Override
    @Transactional
    public void deleteCurrent() {
        userRepo.deleteById(getCurrentUser().getId());
    }

}
