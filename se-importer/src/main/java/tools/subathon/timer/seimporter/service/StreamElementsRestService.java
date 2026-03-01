package tools.subathon.timer.seimporter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class StreamElementsRestService {

    private final String SE_API_URI = "https://api.streamelements.com/kappa/v2";
    private final String CHANNELS_ENDPOINT = "/channels";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    @Autowired
    public StreamElementsRestService(RestClient.Builder restClientBuilder, JsonMapper jsonMapper) {
        this.restClient = restClientBuilder.baseUrl(SE_API_URI).build();
        this.jsonMapper = jsonMapper;
    }

    public String getTwitchChannelId(String streamElementsId) {
        String response = restClient.get()
                .uri(CHANNELS_ENDPOINT + "/{channelId}", streamElementsId)
                .header("accept", "application/json")
                .retrieve().body(String.class);

        try {
            JsonNode node = jsonMapper.readTree(response);
            return node.get("providerId").asString();
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
