package tools.subathon.timer.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.timer.bot.SubathonBot;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_QUEUE;

@Component
public class RpcRequestHandler implements HasLogger {

    private final SubathonBot twitchBot;
    private final ObjectMapper mapper;

    @Autowired
    public RpcRequestHandler(SubathonBot twitchBot, ObjectMapper mapper) {
        this.twitchBot = twitchBot;
        this.mapper = mapper;
    }

    @RabbitListener(queues = CHANNEL_MANAGEMENT_QUEUE)
    public RpcResponseEntity<Boolean> joinChannel(RpcRequestEntity<String> request) {
        getLogger().info("Handling channel management request.");
        if(request == null) {
            return RpcResponseEntity.error("Request is null");
        }
        switch (request.getAction()) {
            case GET, DELETE -> {
                return RpcResponseEntity.of(); // Do nothing for now
            }
            case CREATE_OR_UPDATE -> {
                String channel = mapper.convertValue(request.getBody(), String.class);
                if(StringUtils.isBlank(channel)) {
                    return RpcResponseEntity.error("Channel name is empty!");
                }
                return RpcResponseEntity.of(twitchBot.joinChannel(channel));
            }
            default -> {
                return RpcResponseEntity.error("Unknown request action");
            }
        }
    }
}
