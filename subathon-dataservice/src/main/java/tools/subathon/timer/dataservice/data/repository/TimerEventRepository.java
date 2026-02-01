package tools.subathon.timer.dataservice.data.repository;

import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface TimerEventRepository extends Repository<TimerEventEntity, Long> {

    TimerEventEntity save(TimerEventEntity timerEventEntity);

    Optional<TimerEventEntity> findById(long id);

    TimerEventEntity findFirstByOrderByInsertTimeDescIdDesc();

    TimerEventEntity findFirstByTimerIdOrderByInsertTimeDesc(UUID timerId);

}
