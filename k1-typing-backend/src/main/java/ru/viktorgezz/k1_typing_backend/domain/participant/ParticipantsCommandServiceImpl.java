package ru.viktorgezz.k1_typing_backend.domain.participant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantsCommandServiceImpl implements ParticipantsCommandService {

    private final ParticipantsRepo participantsRepo;

    @Override
    @Transactional
    public Participants save(Participants participants) {
        return participantsRepo.save(participants);
    }

    @Override
    @Transactional
    public void deleteByIdContestAndIdUser(Long idContest, Long idUser) {
        participantsRepo.deleteByIdContestAndIdUser(idContest, idUser);
    }
}
