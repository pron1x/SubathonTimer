package com.pronixxx.subathon.service;

import com.pronixxx.subathon.util.GlobalRabbitMQ;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) throws AmqpException {
        rabbitTemplate.convertAndSend(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.TIMER_EVENT_ROUTING_KEY, message);
    }
}
