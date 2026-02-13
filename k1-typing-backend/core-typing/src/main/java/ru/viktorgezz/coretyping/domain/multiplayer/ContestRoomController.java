package ru.viktorgezz.coretyping.domain.multiplayer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.rq.CreateRoomRqDto;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.rs.AvailableRoomRsDto;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.rs.JoinRoomRsDto;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.rs.RoomInfoRsDto;
import ru.viktorgezz.coretyping.domain.multiplayer.service.intrf.ContestRoomCommandService;
import ru.viktorgezz.coretyping.domain.multiplayer.service.intrf.ContestRoomQueryService;

@RestController
@RequestMapping("/multiplayer/room")
@RequiredArgsConstructor
public class ContestRoomController {

    private final ContestRoomQueryService contestRoomQueryService;
    private final ContestRoomCommandService contestRoomCommandService;

    @PostMapping
    public JoinRoomRsDto create(@Valid @RequestBody CreateRoomRqDto dto) {
        return contestRoomCommandService.createRoom(dto);
    }

    @PostMapping("/{idContest}/join")
    public JoinRoomRsDto join(@PathVariable Long idContest) {
        return contestRoomCommandService.joinRoom(idContest);
    }

    @DeleteMapping("/{idContest}/leave")
    public void leave(@PathVariable Long idContest) {
        contestRoomCommandService.leaveRoom(idContest);
    }

    @GetMapping("/available")
    public PagedModel<AvailableRoomRsDto> getAvailable(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<AvailableRoomRsDto> rooms = contestRoomQueryService.getAvailableRooms(pageable);
        return new PagedModel<>(rooms);
    }

    @GetMapping("/{idContest}")
    public RoomInfoRsDto getInfo(@PathVariable Long idContest) {
        return contestRoomQueryService.getRoomInfo(idContest);
    }
}
