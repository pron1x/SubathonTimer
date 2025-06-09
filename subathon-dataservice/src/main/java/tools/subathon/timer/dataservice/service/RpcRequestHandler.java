package tools.subathon.timer.dataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.user.TwitchAccount;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.BOT_COMMAND_RPC_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_RPC_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_RPC_QUEUE;

@Component
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

    @RabbitListener(queues = USER_CONFIG_RPC_QUEUE)
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

    @RabbitListener(queues = TIMER_RPC_QUEUE)
    public RpcResponseEntity<Boolean> handleTimerRequest(RpcRequestEntity<TwitchAccount> request) {
        getLogger().info("Handling initialize timer request '{}'", request);
        if(request == null) {
            return RpcResponseEntity.error("Request is null");
        }
        switch (request.getAction()) {
            case GET, DELETE -> {
                return RpcResponseEntity.of();
            }
            case CREATE_OR_UPDATE -> {
                TwitchAccount account = mapper.convertValue(request.getBody(), TwitchAccount.class);
                if(account == null) {
                    return RpcResponseEntity.error("Missing channel name!");
                }
                boolean initialized = timerService.initializeTimer(account.getUserId(), account.getChannelName());
                return RpcResponseEntity.of(initialized);
            }
            default -> {
                return RpcResponseEntity.error("Unknown request action");
            }
        }
    }

    @RabbitListener(queues = BOT_COMMAND_RPC_QUEUE)
    public RpcResponseEntity<Boolean> handleBotCommand(RpcRequestEntity<SubathonCommandEvent> request) {
        getLogger().info("Handling bot command request '{}'", request);
        if(request == null) {
            return RpcResponseEntity.error("Request is null");
        }

        switch (request.getAction()) {
            case GET, DELETE -> {
                return RpcResponseEntity.of();
            }
            case CREATE_OR_UPDATE -> {
                String channelId = mapper.convertValue(request.getParams().get("channelId"), String.class);
                SubathonCommandEvent command = mapper.convertValue(request.getBody(), SubathonCommandEvent.class);
                if(command == null) {
                    return RpcResponseEntity.error("Missing command!");
                }
                timerService.executeBotCommand(channelId, command);
                return RpcResponseEntity.of(true);
            }
            default -> {
                return RpcResponseEntity.error("Unknown request action");
            }
        }
    }

}
