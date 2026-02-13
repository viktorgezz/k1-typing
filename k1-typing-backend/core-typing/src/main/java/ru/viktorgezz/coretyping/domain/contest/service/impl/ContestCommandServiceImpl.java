package ru.viktorgezz.coretyping.domain.contest.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.coretyping.domain.contest.Contest;
import ru.viktorgezz.coretyping.domain.contest.Status;
import ru.viktorgezz.coretyping.domain.contest.dto.rq.CreationContestRqDto;
import ru.viktorgezz.coretyping.domain.contest.repo.ContestRepo;
import ru.viktorgezz.coretyping.domain.contest.service.intrf.ContestCommandService;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.coretyping.domain.participant.Participants;
import ru.viktorgezz.coretyping.domain.participant.ParticipantsCommandService;
import ru.viktorgezz.coretyping.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.viktorgezz.coretyping.security.util.CurrentUserUtils.getCurrentUser;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deletePropagationRequiresNew(Long id) {
        contestRepo.deleteById(id);
    }

    @Override
    @Transactional
    public long deleteOldContestsByStatus(LocalDateTime createdBefore, Status status) {
        return contestRepo.deleteOldContestsByStatus(createdBefore, status);
    }

    @Override
    @Transactional
    public void deleteMany(List<Long> ids) {
        contestRepo.deleteAllById(ids);
    }
}
