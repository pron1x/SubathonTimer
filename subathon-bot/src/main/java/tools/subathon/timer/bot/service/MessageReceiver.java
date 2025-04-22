package tools.subathon.timer.bot.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.SubathonBot;
import tools.subathon.timer.util.GlobalRabbitMQ;
import tools.subathon.timer.util.interfaces.HasLogger;

@Service
public class MessageReceiver implements HasLogger {

    private final SubathonBot twitchBot;

    @Autowired
    public MessageReceiver(SubathonBot twitchBot) {
        this.twitchBot = twitchBot;
    }

    @RabbitListener(queues = GlobalRabbitMQ.BOT_RPC_QUEUE_NAME)
    public boolean joinChannel(String channel) {
        getLogger().info("Received message: {}", channel);
        return twitchBot.joinChannel(channel);
    }
}
