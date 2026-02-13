package ru.viktorgezz.coretyping.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.coretyping.auth.dto.UserUpdatedRequest;
import ru.viktorgezz.coretyping.auth.service.UserDataUpdateService;

@RestController
@RequestMapping("/user-update")
@RequiredArgsConstructor
public class UserDataUpdateController {

    private final UserDataUpdateService userDataUpdateService;

    @PutMapping
    public void update(@Valid @RequestBody UserUpdatedRequest dto) {
        userDataUpdateService.update(dto);
    }
}
