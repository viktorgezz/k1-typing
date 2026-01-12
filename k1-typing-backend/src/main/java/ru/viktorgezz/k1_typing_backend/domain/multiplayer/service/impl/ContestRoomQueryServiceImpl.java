package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.AvailableRoomRsDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.RoomInfoRsDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.websocket.AllProgressMessage.UserProgressData;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ProgressService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ReadyService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf.ContestRoomQueryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContestRoomQueryServiceImpl implements ContestRoomQueryService {

    private final ContestQueryService contestQueryService;
    private final ParticipantsService participantsService;
    private final ProgressService progressService;
    private final RoomService roomService;
    private final ReadyService readyService;

    @Override
    @Transactional(readOnly = true)
    public Page<AvailableRoomRsDto> getAvailableRooms(Pageable pageable) {
        return contestQueryService.findByAmountGreaterThanAndStatus(1, Status.CREATED, pageable)
                .map(contest -> new AvailableRoomRsDto(
                        contest.getId(),
                        contest.getExercise().getTitle(),
                        contest.getExercise().getLanguage(),
                        participantsService.getParticipantsCount(contest.getId()),
                        roomService.getParticipantsMax(contest.getId()),
                        contest.getCreatedAt()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomInfoRsDto getRoomInfo(Long idContest) {
        Contest contest = contestQueryService.getOneWithExercise(idContest);

        // Получаем прогресс всех участников (если соревнование идёт)
        Map<Long, UserProgressData> progressAll = (contest.getStatus() == Status.PROGRESS)
                ? progressService.getProgressAll(idContest)
                : Map.of();

        List<RoomInfoRsDto.ParticipantDto> participantDtos = participantsService
                .getParticipantNames(idContest)
                .entrySet()
                .stream()
                .map(entry -> {
                    Long idUser = entry.getKey();
                    UserProgressData progressData = progressAll.getOrDefault(idUser, new UserProgressData(0, 0, BigDecimal.ZERO));
                    return new RoomInfoRsDto.ParticipantDto(
                            idUser,
                                entry.getValue(),
                            readyService.isReady(idContest, idUser),
                            progressData.progress(),
                            progressData.speed(),
                            progressData.accuracy()
                    );
                })
                .toList();

        // Текст упражнения передаём только если соревнование уже началось
        String textExercise = (contest.getStatus() == Status.PROGRESS || contest.getStatus() == Status.FINISHED)
                ? contest.getExercise().getText()
                : null;

        return new RoomInfoRsDto(
                contest.getId(),
                contest.getExercise().getId(),
                contest.getExercise().getTitle(),
                textExercise,
                contest.getExercise().getLanguage().name(),
                contest.getStatus(),
                participantsService.getParticipantsCount(contest.getId()),
                roomService.getParticipantsMax(contest.getId()),
                participantDtos,
                contest.getCreatedAt()
        );
    }
}
