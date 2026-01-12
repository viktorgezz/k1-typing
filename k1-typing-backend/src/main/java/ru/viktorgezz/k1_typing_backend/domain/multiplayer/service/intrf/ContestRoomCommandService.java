package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf;

import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rq.CreateRoomRqDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.JoinRoomRsDto;

public interface ContestRoomCommandService {

    JoinRoomRsDto createRoom(CreateRoomRqDto dto);

    JoinRoomRsDto joinRoom(Long idContest);

    void leaveRoom(Long idContest);
}
