package com.pronixxx.subathon.seimporter;

import com.pronixxx.subathon.seimporter.config.RabbitMQConfig;
import com.pronixxx.subathon.util.GlobalRabbitMQ;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void produceMessage(String message) {
        rabbitTemplate.convertAndSend(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.SUBATHON_ROUTING_KEY, message);
    }
}
