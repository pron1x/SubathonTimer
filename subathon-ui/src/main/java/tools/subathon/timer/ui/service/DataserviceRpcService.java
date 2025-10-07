package tools.subathon.timer.ui.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import tools.subathon.rpc.RpcCommand;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.configuration.ChannelConfigPayload;
import tools.subathon.rpc.payload.configuration.GetChannelConfigPayload;
import tools.subathon.rpc.payload.configuration.UpdateChannelConfigPayload;
import tools.subathon.rpc.payload.timer.InitTimerPayload;
import tools.subathon.rpc.payload.timer.PauseTimerPayload;
import tools.subathon.rpc.payload.timer.StartTimerPayload;
import tools.subathon.rpc.payload.timer.TimerPayload;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;

import java.time.Instant;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_ROUTING_KEY;

@Service
public class DataserviceRpcService {

    private final RabbitTemplate rabbitTemplate;
    private static final String SOURCE = "UI";

    public DataserviceRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public UserConfigurationModel getUserConfiguration(String channelId) {
        RpcRequest<GetChannelConfigPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.GET_CHANNEL_CONFIG);
        request.setPayload(new GetChannelConfigPayload(channelId));
        return sendUserConfigRpcGetRequest(request).getBody();
    }

    public UserConfigurationModel saveUserConfiguration(String userId, UserConfigurationModel userConfigurationModel) {
        RpcRequest<UpdateChannelConfigPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.UPDATE_CHANNEL_CONFIG);
        request.setPayload(new UpdateChannelConfigPayload(userId, userConfigurationModel));
        return sendUserConfigRpcGetRequest(request).getBody();
    }

    public Timer initializeTimerForChannel(String channelId, String channelName) {
        RpcRequest<InitTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.INIT_TIMER);
        request.setPayload(new InitTimerPayload(channelId, channelName, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request).getBody();
    }

    public Timer startTimerForChannel(String channelId, String channelName) {
        RpcRequest<StartTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.START_TIMER);
        request.setPayload(new StartTimerPayload(channelId, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request).getBody();
    }

    public Timer pauseTimerForChannel(String channelId, String channelName) {
        RpcRequest<PauseTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.PAUSE_TIMER);
        request.setPayload(new PauseTimerPayload(channelId, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request).getBody();
    }

    private RpcResponse<UserConfigurationModel> sendUserConfigRpcGetRequest(RpcRequest<? extends ChannelConfigPayload> request) {
        return rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, USER_CONFIG_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
    }

    private RpcResponse<Timer> sendTimerRpcRequest(RpcRequest<? extends TimerPayload> request) {
        return rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, TIMER_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
    }

}
