package ru.viktorgezz.coretyping.domain.participant;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ParticipantsRepo extends CrudRepository<Participants, Long> {

    @Modifying
    @Query("""
            DELETE FROM Participants p
            WHERE p.contest.id = :idContest AND p.user.id = :idUser
            """)
    void deleteByIdContestAndIdUser(@Param("idContest") Long idContest, @Param("idUser") Long idUser);
}
