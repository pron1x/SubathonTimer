package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
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
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.domain.exception.IllegalTimerStateException;
import tools.subathon.timer.dataservice.service.exception.DuplicateTimerException;
import tools.subathon.timer.dataservice.service.exception.InitializationException;
import tools.subathon.timer.dataservice.service.exception.MissingChannelConfigurationException;
import tools.subathon.timer.dataservice.service.exception.MissingTimerException;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.Optional;

import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_RPC_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_RPC_QUEUE;

@Component
public class RpcRequestHandler implements HasLogger {

    private final JsonMapper mapper;
    private final UserConfigurationService userConfigurationService;
    private final TimerService timerService;

    @Autowired
    public RpcRequestHandler(JsonMapper mapper, UserConfigurationService userConfigurationService, TimerService timerService) {
        this.mapper = mapper;
        this.userConfigurationService = userConfigurationService;
        this.timerService = timerService;
    }

    @RabbitListener(queues = USER_CONFIG_RPC_QUEUE)
    public RpcResponse<UserConfigurationDto> handleUserConfigurationRequest(RpcRequest<ChannelConfigPayload> request) {
        getLogger().trace("New user configuration request handler called with request '{}'", request);
        return switch (request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case GET_CHANNEL_CONFIG -> {
                GetChannelConfigPayload payload = (GetChannelConfigPayload) request.getPayload();
                if(payload == null) {
                    yield RpcResponse.error("Request payload is null!");
                }
                Optional<UserConfigurationDto> configOpt = userConfigurationService.getForChannel(payload.channelId());
                yield configOpt.map(RpcResponse::ok).orElseGet(RpcResponse::notFound);
            }
            case UPDATE_CHANNEL_CONFIG -> {
                UpdateChannelConfigPayload payload = (UpdateChannelConfigPayload) request.getPayload();
                UserConfigurationDto config = mapper.convertValue(payload.channelConfig(), UserConfigurationDto.class);
                if(config == null) {
                    yield RpcResponse.error("UserConfiguration is null!");
                }
                try {
                    yield RpcResponse.ok(userConfigurationService.save(config));
                } catch (Exception e) {
                    getLogger().error("Error while saving user configuration {}", payload.channelConfig(), e);
                    yield RpcResponse.error("Error saving user configuration.");
                }
            }
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }

    @RabbitListener(queues = TIMER_RPC_QUEUE)
    public RpcResponse<TimerDto> handleTimerRequest(RpcRequest<TimerPayload> request) {
        getLogger().trace("New timer request handler called with request '{}'", request);
        return switch(request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case GET_TIMER -> {
                GetTimerPayload payload = (GetTimerPayload) request.getPayload();
                TimerDto result = timerService.getLatestTimerForChannel(payload.channelId());
                if(result == null) {
                    yield RpcResponse.notFound();
                }
                yield RpcResponse.ok(result);
            }
            case INIT_TIMER -> {
                InitTimerPayload payload = (InitTimerPayload) request.getPayload();
                TimerDto result;
                try {
                    result = timerService.initializeTimer(payload.channelId(), payload.channelName());
                } catch (MissingChannelConfigurationException | DuplicateTimerException | InitializationException e) {
                    yield RpcResponse.error(e.getMessage());
                }
                if(result == null) {
                    yield RpcResponse.error("Could not initialize timer for channel " + payload.channelId());
                }
                yield RpcResponse.ok(result);
            }
            case START_TIMER -> {
                StartTimerPayload payload = (StartTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                TimerDto result;
                try {
                    result = timerService.startTimer(payload.channelId(), event);
                } catch (MissingTimerException e) {
                    yield RpcResponse.error("No timer for channel " + e.getChannelId() + "found. Cannot " + e.getAction() + " it.");
                } catch (MissingChannelConfigurationException e) {
                    yield RpcResponse.error(e.getMessage());
                }
                if(result == null) {
                    yield RpcResponse.error("Could not start timer for channel " + payload.channelId());
                }
                yield RpcResponse.ok(result);
            }
            case PAUSE_TIMER -> {
                PauseTimerPayload payload = (PauseTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                TimerDto result;
                try {
                    result = timerService.pauseTimer(payload.channelId(), event);
                } catch (MissingTimerException e) {
                    yield RpcResponse.error("No timer for channel " + e.getChannelId() + "found. Cannot " + e.getAction() + " it.");
                } catch (IllegalTimerStateException e) {
                    yield RpcResponse.error("Could not pause timer!");
                }
                if(result == null) {
                    yield RpcResponse.error("Could not pause timer for channel " + payload.channelId());
                }
                yield RpcResponse.ok(result);
            }
            case ADD_TIME -> {
                IncrementTimerPayload payload = (IncrementTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                TimerDto result;
                try {
                    result = timerService.addSubathonEventTime(payload.channelId(), event);
                } catch (MissingTimerException e) {
                    yield RpcResponse.error("No timer for channel " + e.getChannelId() + "found. Cannot " + e.getAction() + " it.");
                }
                if(result == null) {
                    yield RpcResponse.error("Could not add time to timer for channel " + payload.channelId());
                }
                yield RpcResponse.ok(result);
            }
            case SUBTRACT_TIME -> {
                DecrementTimerPayload payload = (DecrementTimerPayload) request.getPayload();
                SubathonCommandEvent event = createFromTimerPayload(payload, payload.source());
                TimerDto result;
                try {
                    result = timerService.subtractSubathonEventTime(payload.channelId(), event);
                } catch (MissingTimerException e) {
                    yield RpcResponse.error("No timer for channel " + e.getChannelId() + "found. Cannot " + e.getAction() + " it.");
                }
                if(result == null) {
                    yield RpcResponse.error("Could not subtract time from timer for channel " + payload.channelId());
                }
                yield RpcResponse.ok(result);
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
