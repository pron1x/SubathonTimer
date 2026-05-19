package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.AmqpException;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.dataservice.data.domain.TimerEvent;
import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import tools.subathon.timer.dataservice.data.mapper.TimerEventMapper;
import tools.subathon.timer.dataservice.data.repository.TimerEventRepository;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TimerEventService implements HasLogger {

    private final RabbitMessageService messageService;
    private final TimerEventMapper mapper;
    private final TimerEventRepository timerEventRepository;

    @Autowired
    public TimerEventService(RabbitMessageService messageService, TimerEventMapper mapper, TimerEventRepository timerEventRepository) {
        this.messageService = messageService;
        this.mapper = mapper;
        this.timerEventRepository = timerEventRepository;
    }

    private TimerEvent save(TimerEvent timerEvent) {
        if (timerEvent.getSubathonEvent() != null && timerEvent.getSubathonEvent().getUsername() == null) {
            timerEvent.getSubathonEvent().setUsername("anonymous");
        }
        TimerEventEntity timerEventEntity = mapper.domainToEntity(timerEvent);
        if (timerEventEntity.getSubathonEvent() == null && timerEvent.getSubathonEvent() != null) {
            getLogger().warn("subathon event got mapped as null!");
        }
        timerEventEntity = timerEventRepository.save(timerEventEntity);
        return mapper.entityToDomain(timerEventEntity);
    }

    public void saveAndPublish(TimerEvent timerEvent) {
        TimerEvent savedEvent = save(timerEvent);
        publishEvent(mapper.domainToDto(savedEvent));
    }

    private void publishEvent(TimerEventDto event) {
        try {
            messageService.sendMessage(event);
        } catch (AmqpException e) {
            getLogger().warn("Could not send message! {}", event, e);
        }
    }

    public boolean hasUserFollowed(UUID timerId, String username) {
        return timerEventRepository.existsFollowEventForUserAndTimer(timerId, username);
    }
}
