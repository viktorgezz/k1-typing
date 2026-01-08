package ru.viktorgezz.k1_typing_backend.domain.participant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantsServiceImpl implements ParticipantsService {

    private final ParticipantsRepo participantsRepo;

    @Override
    @Transactional
    public Participants save(Participants participants) {
        return participantsRepo.save(participants);
    }
}
