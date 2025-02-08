package com.pronixxx.subathon.ui.service;

import com.pronixxx.subathon.datamodel.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DataserviceRestClient {

    private final static String ENDPOINT_TIMER = "/timer";
    private final RestClient restClient;

    @Autowired
    public DataserviceRestClient(RestClient.Builder restClientBuilder, @Value("${ui.endpoints.dataservice}")String requestUri) {
        this.restClient = restClientBuilder.baseUrl(requestUri).build();
    }

    public Timer getTimerForChannel(String twitchId) {
        ResponseEntity<Timer> result = restClient.get()
                .uri(ENDPOINT_TIMER + "/{twitchId}", twitchId)
                .retrieve().toEntity(Timer.class);

        if(result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            return null;
        }
    }

    public List<Timer> getAllActiveTimers() {
        ResponseEntity<List<Timer>> result = restClient.get()
                .uri(ENDPOINT_TIMER + "/all")
                .retrieve().toEntity(new ParameterizedTypeReference<>() {});
        if(result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            return List.of();
        }
    }
}
