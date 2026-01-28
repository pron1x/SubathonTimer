package tools.subathon.timer.ui.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;

import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_EVENT_QUEUE;

@Service
public class MessageReceiver implements HasLogger {

    private final TimerEventService timerEventService;

    @Autowired
    public MessageReceiver(TimerEventService timerEventService) {
        this.timerEventService = timerEventService;
    }

    @RabbitListener(queues = TIMER_EVENT_QUEUE)
    public void receiveMessage(TimerEventDto timerEventDto) {
        timerEventService.handleIncomingTimerEvent(timerEventDto);
    }
}
