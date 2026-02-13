package ru.viktorgezz.coretyping.domain.multiplayer.service.intrf;

import ru.viktorgezz.coretyping.domain.multiplayer.dto.rq.CreateRoomRqDto;
import ru.viktorgezz.coretyping.domain.multiplayer.dto.rs.JoinRoomRsDto;

public interface ContestRoomCommandService {

    JoinRoomRsDto createRoom(CreateRoomRqDto dto);

    JoinRoomRsDto joinRoom(Long idContest);

    void leaveRoom(Long idContest);
}
