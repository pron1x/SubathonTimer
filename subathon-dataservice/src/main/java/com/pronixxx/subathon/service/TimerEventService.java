package com.pronixxx.subathon.service;

import com.pronixxx.subathon.data.entity.TimerEventEntity;
import com.pronixxx.subathon.data.repository.TimerEventRepository;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TimerEventService implements HasLogger {

    private final ModelMapper modelMapper;
    private final TimerEventRepository timerEventRepository;

    @Autowired
    public TimerEventService(ModelMapper modelMapper, TimerEventRepository timerEventRepository) {
        this.modelMapper = modelMapper;
        this.timerEventRepository = timerEventRepository;
    }

    public TimerEvent createNewTimerEvent(long timerId, TimerEventType eventType,
                                          TimerState oldState, TimerState currentState,
                                          Instant oldEnd, Instant currentEnd, SubathonEvent subathonEvent) {
        TimerEvent newEvent = new TimerEvent();

        newEvent.setType(eventType);
        newEvent.setTimerId(timerId);

        newEvent.setOldTimerState(oldState);
        newEvent.setCurrentTimerState(currentState);

        newEvent.setOldEndTime(oldEnd);
        newEvent.setCurrentEndTime(currentEnd);

        newEvent.setSubathonEvent(subathonEvent);
        newEvent.setTimestamp(Instant.now());

        return newEvent;
    }

    public TimerEvent save(TimerEvent timerEvent) {
        TimerEventEntity timerEventEntity = modelMapper.map(timerEvent, TimerEventEntity.class);
        if (timerEventEntity.getSubathonEvent() == null && timerEvent.getSubathonEvent() != null) {
            getLogger().warn("subathon event got mapped as null!");
        }
        timerEventEntity = timerEventRepository.save(timerEventEntity);
        return modelMapper.map(timerEventEntity, TimerEvent.class);
    }
}
