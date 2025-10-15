package tools.subathon.timer.ui.service;

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

    public Timer getTimerForChannel(String channelId) {
        return dataserviceRpcService.getTimerForChannel(channelId);
    }

    public Timer initializeTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    public Timer startTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.startTimerForChannel(channelId, channelName);
    }

    public Timer pauseTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.pauseTimerForChannel(channelId, channelName);
    }

}
