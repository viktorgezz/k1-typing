package ru.viktorgezz.k1_typing_backend.domain.contest.service.impl;

import static ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils.getCurrentUser;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.Status;
import ru.viktorgezz.k1_typing_backend.domain.contest.dto.CreationContestRqDto;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.k1_typing_backend.domain.participant.Participants;
import ru.viktorgezz.k1_typing_backend.domain.participant.ParticipantsService;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

@Service
@RequiredArgsConstructor
public class ContestCommandServiceImpl implements ContestCommandService {

    private final ExerciseQueryService exerciseQueryService;
    private final ParticipantsService participantsService;

    private final ContestRepo contestRepo;

    @Override
    @Transactional
    public Contest createSingle(CreationContestRqDto dto) {
        Exercise exercise = exerciseQueryService.getOne(dto.idExercise());

        Contest contest = contestRepo.save(
                new Contest(Status.PROGRESS, 1, exercise)
        );

        User userCurrent;
        try {
            userCurrent = getCurrentUser();
        } catch (Exception e) {
            userCurrent = null;
        }

        Participants participant = participantsService.save(
                new Participants(contest, userCurrent)
        );
        contest.setParticipants(List.of(participant));

        return contest;
    }

    @Override
    @Transactional
    public Contest delete(Long id) {
        Contest contest = contestRepo.findById(id).orElse(null);
        if (contest != null) {
            contestRepo.delete(contest);
        }
        return contest;
    }

    @Override
    @Transactional
    public void deleteMany(List<Long> ids) {
        contestRepo.deleteAllById(ids);
    }
}
