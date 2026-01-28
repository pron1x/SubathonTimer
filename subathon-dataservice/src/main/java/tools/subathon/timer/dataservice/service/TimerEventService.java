package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.AmqpException;
import tools.subathon.timer.dataservice.data.domain.TimerEvent;
import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import tools.subathon.timer.dataservice.data.repository.TimerEventRepository;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimerEventService implements HasLogger {

    private final RabbitMessageService messageService;
    private final ModelMapper modelMapper;
    private final TimerEventRepository timerEventRepository;

    @Autowired
    public TimerEventService(RabbitMessageService messageService, ModelMapper modelMapper, TimerEventRepository timerEventRepository) {
        this.messageService = messageService;
        this.modelMapper = modelMapper;
        this.timerEventRepository = timerEventRepository;
    }

    public TimerEvent save(TimerEvent timerEvent) {
        TimerEventEntity timerEventEntity = modelMapper.map(timerEvent, TimerEventEntity.class);
        if (timerEventEntity.getSubathonEvent() == null && timerEvent.getSubathonEvent() != null) {
            getLogger().warn("subathon event got mapped as null!");
        }
        timerEventEntity = timerEventRepository.save(timerEventEntity);
        return modelMapper.map(timerEventEntity, TimerEvent.class);
    }

    public void saveAndPublish(TimerEvent timerEvent) {
        TimerEvent savedEvent = save(timerEvent);
        publishEvent(savedEvent);
    }

    public void publishEvent(TimerEvent event) {
        try {
            messageService.sendMessage(event);
        } catch (AmqpException e) {
            getLogger().warn("Could not send message! {}", event, e);
        }
    }
}
