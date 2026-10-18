package tools.subathon.timer.dataservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;
import tools.subathon.timer.dataservice.data.mapper.UserConfigMapper;
import tools.subathon.timer.dataservice.data.repository.UserConfigurationRepository;
import tools.subathon.timer.dataservice.subscriptions.SubscriptionTargetRegistry;

import java.util.Optional;

@Service
public class UserConfigurationService {

    @Value("${timer.seconds.follow}")
    private int FOLLOWER_SECONDS = 10;
    @Value("${timer.seconds.raid}")
    private int RAIDER_SECONDS = 1;
    @Value("${timer.seconds.tier1}")
    private int TIER_1_SECONDS = 300;
    @Value("${timer.seconds.tier2}")
    private int TIER_2_SECONDS = 600;
    @Value("${timer.seconds.tier3}")
    private int TIER_3_SECONDS = 1500;
    @Value("${timer.seconds.tier1-gift}")
    private int TIER_1_GIFT_SECONDS = 300;
    @Value("${timer.seconds.tier2-gift}")
    private int TIER_2_GIFT_SECONDS = 600;
    @Value("${timer.seconds.tier3-gift}")
    private int TIER_3_GIFT_SECONDS = 1500;
    @Value("${timer.seconds.euro}")
    private int EURO_SECONDS = 60; // Seconds added per 100 Euro cents
    @Value("${timer.seconds.bits}")
    private int BITS_SECONDS = 60; // Seconds added per 100 bits
    @Value("${timer.seconds.initial}")
    private int INITIAL_TIMER_SECONDS = 100;

    private final UserConfigMapper mapper;

    private final UserConfigurationRepository userConfigurationRepository;

    private final SubscriptionTargetRegistry subscriptionTargetRegistry;

    public UserConfigurationService(UserConfigMapper mapper, UserConfigurationRepository userConfigurationRepository, SubscriptionTargetRegistry subscriptionTargetRegistry) {
        this.mapper = mapper;
        this.userConfigurationRepository = userConfigurationRepository;
        this.subscriptionTargetRegistry = subscriptionTargetRegistry;
    }

    public UserConfigurationDto save(UserConfigurationDto userConfigurationModel) {
        UserConfigurationEntity config = userConfigurationRepository.save(mapper.dtoToEntity(userConfigurationModel));
        subscriptionTargetRegistry.publishUpdate(config);
        return mapper.entityToDto(config);
    }

    public Optional<UserConfigurationDto> getForChannel(String channelId) {
        UserConfigurationEntity config = userConfigurationRepository.findByChannelId(channelId);
        return config != null ? Optional.of(mapper.entityToDto(config)) : Optional.empty();
    }

    public UserConfigurationDto getDefaultConfiguration() {
        return new UserConfigurationDto(null, null,
                FOLLOWER_SECONDS, RAIDER_SECONDS, TIER_1_SECONDS, TIER_2_SECONDS, TIER_3_SECONDS,
                TIER_1_GIFT_SECONDS, TIER_2_GIFT_SECONDS, TIER_3_GIFT_SECONDS, EURO_SECONDS, BITS_SECONDS, INITIAL_TIMER_SECONDS, null, null, null);
    }

}
