package tools.subathon.timer.bot.service;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwitchChatService {

    @Value("${bot.twitch.user.id}")
    private String botId;

    private final TwitchClient twitchClient;

    @Autowired
    public TwitchChatService(TwitchClient twitchClient) {
        this.twitchClient = twitchClient;
    }

    public void sendChatMessage(String channelId, String message) {
        ChatMessage msg = ChatMessage.builder().senderId(botId).broadcasterId(channelId).message(message).build();
        twitchClient.getHelix().sendChatMessage(null, msg).execute();
    }

    public boolean isChatterOwnBot(String chatterId) {
        return botId.equals(chatterId);
    }
}
