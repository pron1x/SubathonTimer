package tools.subathon.timer.dataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.configuration.ChannelConfigPayload;
import tools.subathon.rpc.payload.configuration.GetChannelConfigPayload;
import tools.subathon.rpc.payload.configuration.UpdateChannelConfigPayload;
import tools.subathon.rpc.payload.timer.DecrementTimerPayload;
import tools.subathon.rpc.payload.timer.GetTimerPayload;
import tools.subathon.rpc.payload.timer.IncrementTimerPayload;
import tools.subathon.rpc.payload.timer.InitTimerPayload;
import tools.subathon.rpc.payload.timer.PauseTimerPayload;
import tools.subathon.rpc.payload.timer.StartTimerPayload;
import tools.subathon.rpc.payload.timer.TimerPayload;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.util.interfaces.HasLogger;

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

    // TODO: Error handling
    @RabbitListener(queues = USER_CONFIG_RPC_QUEUE)
    public RpcResponse<UserConfigurationModel> handleUserConfigurationRequest(RpcRequest<ChannelConfigPayload> request) {
        getLogger().info("New user configuration request handler called with request '{}'", request);
        return switch (request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case GET_CHANNEL_CONFIG -> {
                GetChannelConfigPayload payload = (GetChannelConfigPayload) request.getPayload();
                yield RpcResponse.of(userConfigurationService.getForChannel(payload.channelId()));
            }
            case UPDATE_CHANNEL_CONFIG -> {
                UpdateChannelConfigPayload payload = (UpdateChannelConfigPayload) request.getPayload();
                UserConfigurationModel config = mapper.convertValue(payload.channelConfig(), UserConfigurationModel.class);
                if(config == null) {
                    yield RpcResponse.error("UserConfiguration is null!");
                }
                yield RpcResponse.of(userConfigurationService.save(config));
            }
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }

    // TODO: Error handling
    @RabbitListener(queues = TIMER_RPC_QUEUE)
    public RpcResponse<Timer> handleTimerRequest(RpcRequest<TimerPayload> request) {
        getLogger().info("New timer request handler called with request '{}'", request);
        return switch(request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case GET_TIMER -> {
                GetTimerPayload payload = (GetTimerPayload) request.getPayload();
                yield RpcResponse.of(timerService.getLatestTimerForChannel(payload.channelId()));
            }
            case INIT_TIMER -> {
                InitTimerPayload payload = (InitTimerPayload) request.getPayload();
                yield RpcResponse.of(timerService.initializeTimer(payload.channelId(), payload.channelName()));
            }
            case START_TIMER -> {
                StartTimerPayload payload = (StartTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                yield RpcResponse.of(timerService.startTimer(payload.channelId(), event));
            }
            case PAUSE_TIMER -> {
                PauseTimerPayload payload = (PauseTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                yield RpcResponse.of(timerService.pauseTimer(payload.channelId(), event));
            }
            case ADD_TIME -> {
                IncrementTimerPayload payload = (IncrementTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                yield RpcResponse.of(timerService.addSubathonEventTime(payload.channelId(), event));
            }
            case SUBTRACT_TIME -> {
                DecrementTimerPayload payload = (DecrementTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                yield RpcResponse.of(timerService.subtractSubathonEventTime(payload.channelId(), event));
            }
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }

    private SubathonCommandEvent createFromTimerPayload(TimerPayload payload, String source) {
        if(payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if(payload instanceof GetTimerPayload) {
            throw new IllegalArgumentException("GetTimerPayload cannot be converted to SubathonCommandEvent");
        }
        SubathonCommandEvent event = new SubathonCommandEvent();
        switch (payload) {
            case InitTimerPayload init -> {
                event.setCommand(Command.INIT);
                event.setUsername(init.username());
                event.setTimestamp(init.timestamp());
                event.setSource(source);
            }
            case StartTimerPayload start -> {
                event.setCommand(Command.START);
                event.setUsername(start.username());
                event.setTimestamp(start.timestamp());
                event.setSource(source);
            }
            case PauseTimerPayload pause -> {
                event.setCommand(Command.PAUSE);
                event.setUsername(pause.username());
                event.setTimestamp(pause.timestamp());
                event.setSource(source);
            }
            case IncrementTimerPayload increment -> {
                event.setCommand(Command.ADD);
                event.setUsername(increment.username());
                event.setSeconds(increment.seconds());
                event.setTimestamp(increment.timestamp());
                event.setSource(source);
            }
            case DecrementTimerPayload decrement -> {
                event.setCommand(Command.REMOVE);
                event.setUsername(decrement.username());
                event.setSeconds(decrement.seconds());
                event.setTimestamp(decrement.timestamp());
                event.setSource(source);
            }
            default -> throw new IllegalArgumentException("SubathonCommandEvent creation not supported for this payload type.");
        }
        return event;
    }

}
