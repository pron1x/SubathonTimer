package tools.subathon.timer.seimporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.seimporter.factory.SubathonEventFactory;
import tools.subathon.timer.seimporter.model.StreamElementsEventModel;
import tools.subathon.timer.seimporter.service.RabbitMessageService;
import tools.subathon.timer.seimporter.service.TwitchIdCacheService;
import tools.subathon.timer.util.interfaces.HasLogger;
import io.socket.client.IO;
import io.socket.client.Socket;
import jakarta.annotation.PostConstruct;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class SocketService implements HasLogger {

    private final ObjectMapper objectMapper;

    private final RabbitMessageService messageService;

    private final TwitchIdCacheService twitchIdCacheService;

    @Value("${seimporter.socket.streamelements.baseurl}")
    private String url;
    @Value("${seimporter.socket.auth.jwt}")
    private List<String> jwtList;
    private Socket socket;

    @Autowired
    public SocketService(ObjectMapper objectMapper, RabbitMessageService messageService, TwitchIdCacheService twitchIdCacheService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.twitchIdCacheService = twitchIdCacheService;
    }


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
        socket.on("authenticated", this::onAuthenticated);
        socket.on("unauthorized", this::onUnauthorized);
        socket.on("event", this::onEvent);
    }

    private void onConnect() {
        getLogger().info("Connected to socket.");
        getLogger().info("Trying to authenticate with {} tokens.", jwtList.size());
        for(String token : jwtList) {
            JSONObject authObject = new JSONObject();
            try {
                authObject.put("method", "jwt");
                authObject.put("token", token);
            } catch (JSONException e) {
                getLogger().error("Unable to create authentication json object. {}", e.getMessage());
                return;
            }
            socket.emit("authenticate", authObject);
        }
        getLogger().info("sent all authenticate messages.");
    }

    private void onDisconnect() {
        getLogger().info("Disconnected from socket.");
    }

    private void onAuthenticated(Object ...e) {
        if(e.length > 0) {
            try {
                JsonNode node = objectMapper.readTree(e[0].toString());
                String streamElementsId = node.get("channelId").asText();
                String twitchId = twitchIdCacheService.getTwitchId(streamElementsId);
                getLogger().info("Authenticated on StreamElements for channel '{}' (Twitch: '{}')", streamElementsId, twitchId);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
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
        getLogger().info("Received StreamElements event: {}.", events[0].toString());
        StreamElementsEventModel event;
        try {
            event = objectMapper.readValue(events[0].toString(), StreamElementsEventModel.class);
            getLogger().info(event.toString());
            SubathonEvent subathonEvent = SubathonEventFactory.convertToSubathonEvent(event);
            SubathonEventMessage eventMessage = new SubathonEventMessage();
            eventMessage.setChannelId(twitchIdCacheService.getTwitchId(event.getChannel()));
            eventMessage.setSubathonEvent(subathonEvent);
            messageService.produceMessage(objectMapper.writeValueAsString(eventMessage));
        } catch (Exception e) {
            getLogger().warn("Unable to map event to event model! Event= {}", events[0], e);
        }
    }

}
