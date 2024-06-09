package com.pronixxx.subathon.seimporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.seimporter.factory.SubathonEventFactory;
import com.pronixxx.subathon.seimporter.model.StreamElementsEventModel;
import io.socket.client.IO;
import io.socket.client.Socket;
import jakarta.annotation.PostConstruct;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class SocketService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitMessageService messageService;

    @Value("${seimporter.socket.streamelements.baseurl}")
    private String url;
    @Value("${seimporter.socket.auth.jwt}")
    private String jwt;
    private Socket socket;


    @PostConstruct
    public void postConstruct() {
        IO.Options options = IO.Options.builder()
                .setTransports(new String[]{"websocket"})
                .build();
        socket = IO.socket(URI.create(url), options);
        init();
        start();
    }

    public void start() {
        socket.connect();
    }

    private void init() {
        socket.on("connect", e -> onConnect());
        socket.on("disconnect", e -> onDisconnect());
        socket.on("authenticated", e -> onAuthenticated());
        socket.on("unauthorized", this::onUnauthorized);
        socket.on("event", this::onEvent);
    }

    private void onConnect() {
        getLogger().info("Connected to socket.");
        JSONObject authObject = new JSONObject();
        try {
            authObject.put("method", "jwt");
            authObject.put("token", jwt);
        } catch (JSONException e) {
            getLogger().error("Unable to create authentication json object. {}", e.getMessage());
            return;
        }
        socket.emit("authenticate", authObject);
        getLogger().info("sent authenticate.");
    }

    private void onDisconnect() {
        getLogger().info("Disconnected from socket.");
    }

    private void onAuthenticated() {
        getLogger().info("Authenticated.");
    }

    private void onUnauthorized(Object... e) {
        getLogger().error("Could not authorize with socket!");
        for(Object o : e) {
            getLogger().info(o.toString());
        }
    }

    private void onEvent(Object... events) {
        if(events.length == 0) return;
        StreamElementsEventModel event;
        try {
            event = objectMapper.readValue(events[0].toString(), StreamElementsEventModel.class);
            getLogger().info(event.toString());
            SubathonEvent subathonEvent = SubathonEventFactory.convertToSubathonEvent(event);
            messageService.produceMessage(objectMapper.writeValueAsString(subathonEvent));
        } catch (Exception e) {
            getLogger().warn("Unable to map event to event model! Event= {}", events[0]);
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(SocketService.class.getSimpleName());
    }
}
