package tools.subathon.timer.dataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.enums.EventType;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_RPC_QUEUE_NAME;

@Component
@RabbitListener(queues = DATASERVICE_RPC_QUEUE_NAME)
public class RpcRequestHandler implements HasLogger {

    private final ObjectMapper mapper;
    private final UserConfigurationService userConfigurationService;
    private final TimerService timerService;

    @Autowired
    public RpcRequestHandler(ObjectMapper mapper, UserConfigurationService userConfigurationService, TimerService timerService) {
        this.mapper = mapper;
        this.userConfigurationService = userConfigurationService;
        this.timerService = timerService;
    }

    @RabbitHandler
    public RpcResponseEntity<UserConfigurationModel> handleUserConfigurationRequest(RpcRequestEntity<UserConfigurationModel> request) {
        getLogger().debug("Handling user configuration request '{}'", request);
        if(request == null) {
            return RpcResponseEntity.error("Request is null");
        }
        String userId = (String) request.getParams().get("userId");
        switch (request.getAction()) {
            case GET -> {
                if (userId == null) {
                    return RpcResponseEntity.error("UserId parameter is required");
                }
                return RpcResponseEntity.of(userConfigurationService.getForChannel(userId));
            }
            case CREATE_OR_UPDATE -> {
                UserConfigurationModel config = mapper.convertValue(request.getBody(),  UserConfigurationModel.class);
                if(config == null) {
                    return RpcResponseEntity.error("UserConfiguration is null!");
                }
                return RpcResponseEntity.of(userConfigurationService.save(config));
            }
            case DELETE -> {
                return RpcResponseEntity.of(userConfigurationService.deleteForChannel(userId));
            }
            default -> {
                return RpcResponseEntity.error("Unknown request action");
            }
        }
    }

    @RabbitHandler
    public Boolean handleBotCommand(SubathonEventMessage eventMessage) {
        SubathonEvent command = eventMessage.getSubathonEvent();
        if (command.getType() != EventType.COMMAND) {
            return false;
        }
        timerService.executeBotCommand(eventMessage.getChannelId(), (SubathonCommandEvent) command);
        return true;
    }

}
