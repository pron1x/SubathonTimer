package com.pronixxx.subathon.ui.service;

import com.pronixxx.subathon.datamodel.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimerService {

    private final DataserviceRestClient dataserviceRestClient;

    @Autowired
    public TimerService(DataserviceRestClient dataserviceRestClient) {
        this.dataserviceRestClient = dataserviceRestClient;
    }

    public Timer getTimerForChannel(String channelId) {
        return dataserviceRestClient.getLatestTimerForChannel(channelId);
    }

    public List<Timer> getAllActiveTimers() {
        return dataserviceRestClient.getAllActiveTimers();
    }
}
