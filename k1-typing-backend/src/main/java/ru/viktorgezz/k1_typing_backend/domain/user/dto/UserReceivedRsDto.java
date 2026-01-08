package ru.viktorgezz.k1_typing_backend.domain.user.dto;

import ru.viktorgezz.k1_typing_backend.domain.user.Role;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

public record UserReceivedRsDto(
        String username,
        Role role
) {
    public UserReceivedRsDto(User user) {
        this(user.getUsername(), user.getRole());
    }
}
