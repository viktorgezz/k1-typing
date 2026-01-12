package ru.viktorgezz.k1_typing_backend.domain.contest.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.contest.Contest;
import ru.viktorgezz.k1_typing_backend.domain.contest.repo.ContestRepo;
import ru.viktorgezz.k1_typing_backend.domain.contest.dto.rq.CreationContestRqDto;
import ru.viktorgezz.k1_typing_backend.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.k1_typing_backend.domain.participant.Participants;
import ru.viktorgezz.k1_typing_backend.domain.participant.ParticipantsCommandService;
import ru.viktorgezz.k1_typing_backend.domain.user.User;

import java.util.List;

import static ru.viktorgezz.k1_typing_backend.security.util.CurrentUserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class ContestCommandServiceImpl implements ContestCommandService {

    private final ExerciseQueryService exerciseQueryService;
    private final ParticipantsCommandService participantsCommandService;

    private final ContestRepo contestRepo;

    @Override
    @Transactional
    public Contest createSingle(CreationContestRqDto dto) {
        Exercise exercise = exerciseQueryService.getOne(dto.idExercise());

        Contest contest = contestRepo.save(
                new Contest(dto.status(), 1, exercise)
        );

        User userCurrent;
        try {
            userCurrent = getCurrentUser();
        } catch (Exception e) {
            userCurrent = null;
        }

        Participants participant = participantsCommandService.save(
                new Participants(contest, userCurrent)
        );
        contest.setParticipants(List.of(participant));

        return contest;
    }

    @Override
    public Contest save(Contest contest) {
        return contestRepo.save(contest);
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
