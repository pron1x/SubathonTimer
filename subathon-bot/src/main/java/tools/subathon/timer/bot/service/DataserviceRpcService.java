package tools.subathon.timer.bot.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import tools.subathon.rpc.RpcCommand;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.timer.TimerPayload;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.Timer;

import static tools.subathon.timer.util.GlobalRabbitMQ.BOT_COMMAND_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class DataserviceRpcService {

    private final RabbitTemplate rabbitTemplate;

    public DataserviceRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Timer executeBotCommand(String channelId, SubathonCommandEvent command) {
        RpcRequest<TimerPayload> request = new RpcRequest<>();
        TimerPayload payload = createPayloadFromSubathonCommandEvent(channelId, command);
        request.setPayload(payload);
        request.setCommand(switch (command.getCommand()) {
            case INIT -> RpcCommand.INIT_TIMER;
            case START -> RpcCommand.START_TIMER;
            case PAUSE -> RpcCommand.PAUSE_TIMER;
            case ADD -> RpcCommand.ADD_TIME;
            case REMOVE -> RpcCommand.SUBTRACT_TIME;
        });
        return sendTimerRpcRequest(request).getBody();
    }

    private RpcResponse<Timer> sendTimerRpcRequest(RpcRequest<? extends TimerPayload> request) {
        return rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, BOT_COMMAND_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
    }

    private TimerPayload createPayloadFromSubathonCommandEvent(String channelId, SubathonCommandEvent command) {
        if(command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        return switch (command.getCommand()) {
            case INIT -> new tools.subathon.rpc.payload.timer.InitTimerPayload(
                    channelId,
                    command.getUsername(),
                    command.getUsername(),
                    command.getTimestamp(),
                    command.getSource()
            );
            case START -> new tools.subathon.rpc.payload.timer.StartTimerPayload(
                    channelId,
                    command.getUsername(),
                    command.getTimestamp(),
                    command.getSource()
            );
            case PAUSE -> new tools.subathon.rpc.payload.timer.PauseTimerPayload(
                    channelId,
                    command.getUsername(),
                    command.getTimestamp(),
                    command.getSource()
            );
            case ADD -> new tools.subathon.rpc.payload.timer.IncrementTimerPayload(
                    channelId,
                    command.getSeconds(),
                    command.getUsername(),
                    command.getTimestamp(),
                    command.getSource()
            );
            case REMOVE ->  new tools.subathon.rpc.payload.timer.DecrementTimerPayload(
                    channelId,
                    command.getSeconds(),
                    command.getUsername(),
                    command.getTimestamp(),
                    command.getSource()
            );
        };
    }

}
