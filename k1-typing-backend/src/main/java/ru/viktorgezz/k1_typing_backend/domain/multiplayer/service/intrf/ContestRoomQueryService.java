package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.AvailableRoomRsDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.RoomInfoRsDto;


public interface ContestRoomQueryService {

    Page<AvailableRoomRsDto> getAvailableRooms(Pageable pageable);

    RoomInfoRsDto getRoomInfo(Long idContest);
}
