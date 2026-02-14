package ru.viktorgezz.avatarmodule.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.avatarmodule.entity.Avatar;

import java.util.List;
import java.util.Optional;

public interface AvatarRepo extends CrudRepository<Avatar, Long> {

    Optional<Avatar> findByIdUser(Long idUser);

    List<Avatar> findAllByIdUserIn(Iterable<Long> iterable);
}
