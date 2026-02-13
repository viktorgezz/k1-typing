package ru.viktorgezz.coretyping.domain.participant;

public interface ParticipantsCommandService {

    Participants save(Participants participants);

    void deleteByIdContestAndIdUser(Long idContest, Long idUser);
}
