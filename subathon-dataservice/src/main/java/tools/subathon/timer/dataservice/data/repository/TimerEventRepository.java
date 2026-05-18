package tools.subathon.timer.dataservice.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TimerEventRepository extends Repository<TimerEventEntity, Long> {

    TimerEventEntity save(TimerEventEntity timerEventEntity);

    /**
     * Checks if a FOLLOW event already exists for a specific username for a specific timer
     * @param timerId the timer UUID
     * @param username the username to check
     * @return Whether the user already created a follow event
     */
    @Query("""
            SELECT COUNT(te) > 0
            FROM TimerEventEntity te
            JOIN te.subathonEvent e
            WHERE te.timerId = :timerId
            AND e.username = :username
            AND TYPE(e) = FollowEntity""")
    boolean existsFollowEventForUserAndTimer(@Param("timerId") UUID timerId, @Param("username") String username);

}
