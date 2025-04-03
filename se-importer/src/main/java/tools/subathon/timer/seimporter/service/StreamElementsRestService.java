package tools.subathon.timer.seimporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StreamElementsRestService {

    private final String SE_API_URI = "https://api.streamelements.com/kappa/v2";
    private final String CHANNELS_ENDPOINT = "/channels";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public StreamElementsRestService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(SE_API_URI).build();
        this.objectMapper = objectMapper;
    }

    public String getTwitchChannelId(String streamElementsId) {
        String response = restClient.get()
                .uri(CHANNELS_ENDPOINT + "/{channelId}", streamElementsId)
                .header("accept", "application/json")
                .retrieve().body(String.class);

        try {
            JsonNode node = objectMapper.readTree(response);
            return node.get("providerId").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
