package tools.subathon.timer.ui.service;

import tools.subathon.rpc.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.TimerDto;

@Service
public class TimerService {

    private final DataserviceRpcService dataserviceRpcService;

    @Autowired
    public TimerService(DataserviceRpcService dataserviceRpcService) {
        this.dataserviceRpcService = dataserviceRpcService;
    }

    public RpcResponse<TimerDto> getTimerForChannel(String channelId) {
        return dataserviceRpcService.getTimerForChannel(channelId);
    }

    public RpcResponse<TimerDto> initializeTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    public RpcResponse<TimerDto> startTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.startTimerForChannel(channelId, channelName);
    }

    public RpcResponse<TimerDto> pauseTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.pauseTimerForChannel(channelId, channelName);
    }

}
