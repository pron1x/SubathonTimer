package com.pronixxx.subathon.data.repository;

import com.pronixxx.subathon.data.entity.TimerEventEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TimerEventRepository extends Repository<TimerEventEntity, Long> {

    TimerEventEntity save(TimerEventEntity timerEventEntity);

    Optional<TimerEventEntity> findById(long id);

}
