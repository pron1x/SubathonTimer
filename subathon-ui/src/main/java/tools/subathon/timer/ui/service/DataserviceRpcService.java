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
import tools.subathon.rpc.payload.timer.GetTimerPayload;
import tools.subathon.rpc.payload.timer.InitTimerPayload;
import tools.subathon.rpc.payload.timer.PauseTimerPayload;
import tools.subathon.rpc.payload.timer.StartTimerPayload;
import tools.subathon.rpc.payload.timer.TimerPayload;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.time.Instant;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_ROUTING_KEY;

@Service
public class DataserviceRpcService implements HasLogger {

    private final RabbitTemplate rabbitTemplate;
    private static final String SOURCE = "UI";

    public DataserviceRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public RpcResponse<UserConfigurationModel> getUserConfiguration(String channelId) {
        RpcRequest<GetChannelConfigPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.GET_CHANNEL_CONFIG);
        request.setPayload(new GetChannelConfigPayload(channelId));
        return sendUserConfigRpcGetRequest(request);
    }

    public RpcResponse<UserConfigurationModel> saveUserConfiguration(String userId, UserConfigurationModel userConfigurationModel) {
        RpcRequest<UpdateChannelConfigPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.UPDATE_CHANNEL_CONFIG);
        request.setPayload(new UpdateChannelConfigPayload(userId, userConfigurationModel));
        return sendUserConfigRpcGetRequest(request);
    }

    public RpcResponse<Timer> getTimerForChannel(String channelId) {
        RpcRequest<GetTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.GET_TIMER);
        request.setPayload(new GetTimerPayload(channelId));
        return sendTimerRpcRequest(request);
    }

    public RpcResponse<Timer> initializeTimerForChannel(String channelId, String channelName) {
        RpcRequest<InitTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.INIT_TIMER);
        request.setPayload(new InitTimerPayload(channelId, channelName, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request);
    }

    public RpcResponse<Timer> startTimerForChannel(String channelId, String channelName) {
        RpcRequest<StartTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.START_TIMER);
        request.setPayload(new StartTimerPayload(channelId, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request);
    }

    public RpcResponse<Timer> pauseTimerForChannel(String channelId, String channelName) {
        RpcRequest<PauseTimerPayload> request = new RpcRequest<>();
        request.setCommand(RpcCommand.PAUSE_TIMER);
        request.setPayload(new PauseTimerPayload(channelId, channelName, Instant.now(), SOURCE));
        return sendTimerRpcRequest(request);
    }

    private RpcResponse<UserConfigurationModel> sendUserConfigRpcGetRequest(RpcRequest<? extends ChannelConfigPayload> request) {
        RpcResponse<UserConfigurationModel> response = rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, USER_CONFIG_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
        if(response == null) {
            return RpcResponse.timeout();
        }
        return response;
    }

    private RpcResponse<Timer> sendTimerRpcRequest(RpcRequest<? extends TimerPayload> request) {
        RpcResponse<Timer> response = rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, TIMER_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
        if(response == null) {
            return RpcResponse.timeout();
        }
        return response;
    }

}
