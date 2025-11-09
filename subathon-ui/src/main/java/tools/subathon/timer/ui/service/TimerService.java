package tools.subathon.timer.ui.service;

import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.datamodel.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimerService {

    private final DataserviceRpcService dataserviceRpcService;

    @Autowired
    public TimerService(DataserviceRpcService dataserviceRpcService) {
        this.dataserviceRpcService = dataserviceRpcService;
    }

    public RpcResponse<Timer> getTimerForChannel(String channelId) {
        return dataserviceRpcService.getTimerForChannel(channelId);
    }

    public RpcResponse<Timer> initializeTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    public RpcResponse<Timer> startTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.startTimerForChannel(channelId, channelName);
    }

    public RpcResponse<Timer> pauseTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.pauseTimerForChannel(channelId, channelName);
    }

}
