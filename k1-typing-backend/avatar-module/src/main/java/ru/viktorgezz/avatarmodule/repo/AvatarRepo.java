package ru.viktorgezz.avatarmodule.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.avatarmodule.entity.Avatar;

public interface AvatarRepo extends CrudRepository<Avatar, Long> {
}
