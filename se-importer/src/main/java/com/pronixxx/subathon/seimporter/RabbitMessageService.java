package com.pronixxx.subathon.seimporter;

import com.pronixxx.subathon.seimporter.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void produceMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }
}
