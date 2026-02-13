package ru.viktorgezz.coretyping.domain.user.dto;

import ru.viktorgezz.coretyping.domain.user.Role;
import ru.viktorgezz.coretyping.domain.user.User;

public record UserReceivedRsDto(
        String username,
        Role role
) {
    public UserReceivedRsDto(User user) {
        this(user.getUsername(), user.getRole());
    }
}
