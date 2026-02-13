package ru.viktorgezz.coretyping.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.domain.user.repo.UserRepo;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserQueryService;

import java.util.Optional;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public User getOne(Long id) {
        Optional<User> userOptional = userRepo.findById(id);
        return userOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @Override
    public User getMyself() {
        return getCurrentUser();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findUserByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public User getByUsername(String username) {
        return userRepo.findUserByUsername(username);
    }
}
