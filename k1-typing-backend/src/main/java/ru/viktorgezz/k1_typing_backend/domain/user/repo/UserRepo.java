package ru.viktorgezz.k1_typing_backend.domain.user.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

public interface UserRepo extends CrudRepository<User, Long> {

    User findUserByUsername(String username);
}
