package com.pronixxx.subathon.data.repository;

import com.pronixxx.subathon.data.entity.TimerEntity;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface TimerRepository extends Repository<TimerEntity, Long> {

    TimerEntity save(TimerEntity timerEntity);

    List<TimerEntity> findAll();
    Optional<TimerEntity> findById(long id);
    Optional<TimerEntity> findByChannelNameAndState(String channelName, TimerState timerState);
    List<TimerEntity> findByStateIsNot(TimerState timerState);

}
