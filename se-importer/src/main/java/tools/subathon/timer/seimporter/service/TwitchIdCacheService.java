package tools.subathon.timer.seimporter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TwitchIdCacheService {

    private final StreamElementsRestService streamElementsRestService;

    private final Map<String, String> seIdTwitchIdMap;

    @Autowired
    public TwitchIdCacheService(StreamElementsRestService streamElementsRestService) {
        this.streamElementsRestService = streamElementsRestService;
        seIdTwitchIdMap = new HashMap<>();
    }

    public String getTwitchId(String streamElementsId) {
        String twitchId = seIdTwitchIdMap.get(streamElementsId);
        if(twitchId == null) {
            twitchId = streamElementsRestService.getTwitchChannelId(streamElementsId);
            seIdTwitchIdMap.put(streamElementsId, twitchId);
        }
        return twitchId;
    }

}
