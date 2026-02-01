package tools.subathon.timer.dataservice.data.repository;

import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import org.springframework.data.repository.Repository;

public interface TimerEventRepository extends Repository<TimerEventEntity, Long> {

    TimerEventEntity save(TimerEventEntity timerEventEntity);

}
