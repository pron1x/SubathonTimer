package com.pronixxx.subathon.ui.service;

import com.pronixxx.subathon.datamodel.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimerService {

    private final DataserviceRestClient dataserviceRestClient;

    @Autowired
    public TimerService(DataserviceRestClient dataserviceRestClient) {
        this.dataserviceRestClient = dataserviceRestClient;
    }

    public Timer getTimerForChannel(String channelId) {
        return dataserviceRestClient.getTimerForChannel(channelId);
    }
}
