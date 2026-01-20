package ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.impl;

import static ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils.getCurrentUser;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestQueryService;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rq.CreateRoomRqDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.dto.rs.JoinRoomRsDto;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.redis.service.intrf.RoomService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf.ContestRoomCommandService;
import ru.viktorgezz.k1_typing_backend.domain.multiplayer.service.intrf.ContestWebSocketService;
import ru.viktorgezz.k1_typing_backend.domain.participant.Participants;
import ru.viktorgezz.k1_typing_backend.domain.participant.ParticipantsCommandService;
import ru.viktorgezz.k1_typing_backend.domain.user.User;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ContestRoomCommandServiceImpl implements ContestRoomCommandService {

    private final ExerciseQueryService exerciseQueryService;
    private final ContestQueryService contestQueryService;
    private final ContestCommandService contestCommandService;
    private final ParticipantsCommandService participantsCommandService;
    private final ContestWebSocketService contestWebSocketService;

    private final RoomService roomService;
    private final ParticipantsService participantsService;

    @Override
    @Transactional
    public JoinRoomRsDto createRoom(CreateRoomRqDto dto) {
        User userCurrent = getCurrentUser();
        Exercise exercise = exerciseQueryService.getOne(dto.idExercise());

        Contest contestNew = contestCommandService.save(
                new Contest(Status.CREATED, dto.maxParticipants(), exercise)
        );

        roomService.createRoom(contestNew.getId(), exercise.getId(), dto.maxParticipants());
        participantsService.addParticipant(contestNew.getId(), userCurrent.getId(), userCurrent.getUsername());

        Participants participantsNew = participantsCommandService.save(
                new Participants(contestNew, userCurrent)
        );
        contestNew.setParticipants(List.of(participantsNew));

        return new JoinRoomRsDto(
                contestNew.getId(),
                JoinRoomRsDto.JoinRoomStatus.SUCCESS,
                "Комната успешно создана"
        );
    }

    @Override
    @Transactional
    public JoinRoomRsDto joinRoom(Long idContest) {
        User user = getCurrentUser();

        // Проверяем, является ли пользователь уже участником (через Redis)
        if (participantsService.isParticipant(idContest, user.getId())) {
            return new JoinRoomRsDto(
                    idContest,
                    JoinRoomRsDto.JoinRoomStatus.SUCCESS,
                    "Переподключение к комнате"
            );
        }

        // Проверяем комнату для новых участников
        Contest contest = checkRoomForNewParticipant(idContest);

        participantsService.addParticipant(idContest, user.getId(), user.getUsername());
        participantsCommandService.save(new Participants(contest, user));

        contestWebSocketService.broadcastPlayerJoined(idContest, user.getId(), user.getUsername());

        return new JoinRoomRsDto(
                idContest,
                JoinRoomRsDto.JoinRoomStatus.SUCCESS,
                "Успешно присоединились к комнате"
        );
    }

    @Override
    @Transactional
    public void leaveRoom(Long idContest) {
        User user = getCurrentUser();
        participantsCommandService.deleteByIdContestAndIdUser(idContest, user.getId());
        participantsService.removeParticipant(idContest, user.getId());

        contestWebSocketService.broadcastPlayerLeft(idContest, user.getId(), user.getUsername());
    }

    private Contest checkRoomForNewParticipant(Long idContest) {
        if (!isRoomExistsRedis(idContest)) {
            if (contestQueryService.hasOldContest(idContest)) {
                contestCommandService.delete(idContest);
            }
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND, idContest.toString());
        }

        Contest contest = contestQueryService.getOne(idContest);

        // Новые участники могут присоединяться только к комнатам со статусом CREATED
        if (contest.getStatus() != Status.CREATED) {
            throw new BusinessException(ErrorCode.CONTEST_ALREADY_STARTED, idContest.toString());
        }

        if (participantsService.isRoomFull(idContest)) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_FULL, idContest.toString());
        }

        return contest;
    }

    private boolean isRoomExistsRedis(Long idContest) {
        return roomService.roomExists(idContest);
    }
}
