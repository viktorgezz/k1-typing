package ru.viktorgezz.coretyping.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.coretyping.domain.user.dto.UserReceivedRsDto;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserCommandService;
import ru.viktorgezz.coretyping.domain.user.service.intrf.UserQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @GetMapping("/myself")
    public UserReceivedRsDto getMyself(){
        return new UserReceivedRsDto(userQueryService.getMyself());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserReceivedRsDto getOne(@PathVariable Long id) {
        return new UserReceivedRsDto(userQueryService.getOne(id));
    }

    @DeleteMapping("/myself")
    public void deleteMyself() {
        userCommandService.deleteCurrent();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User delete(@PathVariable Long id) {
        return userCommandService.delete(id);
    }
}
