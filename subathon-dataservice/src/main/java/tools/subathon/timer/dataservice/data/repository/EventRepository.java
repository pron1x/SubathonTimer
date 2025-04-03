package tools.subathon.timer.dataservice.data.repository;

import tools.subathon.timer.dataservice.data.entity.EventEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface EventRepository extends Repository<EventEntity, Long> {

    EventEntity save(EventEntity event);

    Optional<EventEntity> findById(long id);
}
