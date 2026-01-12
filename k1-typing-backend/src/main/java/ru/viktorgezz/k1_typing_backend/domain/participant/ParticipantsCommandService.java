package ru.viktorgezz.k1_typing_backend.domain.participant;

public interface ParticipantsCommandService {

    Participants save(Participants participants);

    void deleteByIdContestAndIdUser(Long idContest, Long idUser);
}
