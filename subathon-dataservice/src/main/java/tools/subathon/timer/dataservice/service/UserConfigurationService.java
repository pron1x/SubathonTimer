package tools.subathon.timer.dataservice.service;

import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;
import tools.subathon.timer.dataservice.data.mapper.UserConfigMapper;
import tools.subathon.timer.dataservice.data.repository.UserConfigurationRepository;
import tools.subathon.timer.dataservice.properties.TimerSecondsProperties;
import tools.subathon.timer.dataservice.subscriptions.SubscriptionTargetRegistry;

import java.util.Optional;

@Service
public class UserConfigurationService {

    private final TimerSecondsProperties timerProperties;

    private final UserConfigMapper mapper;

    private final UserConfigurationRepository userConfigurationRepository;

    private final SubscriptionTargetRegistry subscriptionTargetRegistry;

    public UserConfigurationService(TimerSecondsProperties timerProperties, UserConfigMapper mapper, UserConfigurationRepository userConfigurationRepository, SubscriptionTargetRegistry subscriptionTargetRegistry) {
        this.timerProperties = timerProperties;
        this.mapper = mapper;
        this.userConfigurationRepository = userConfigurationRepository;
        this.subscriptionTargetRegistry = subscriptionTargetRegistry;
    }

    public UserConfigurationDto save(UserConfigurationDto userConfigurationModel) {
        UserConfigurationEntity config = userConfigurationRepository.save(mapper.dtoToEntity(userConfigurationModel));
        subscriptionTargetRegistry.publishUpdate(config);
        return mapper.entityToDto(config);
    }

    public UserConfigurationDto getOrCreateForChannel(String channelId) {
        Optional<UserConfigurationDto> config = getForChannel(channelId);
        if (config.isEmpty()) {
            UserConfigurationEntity newConfig = createDefaultWithChannelId(channelId);
            return save(mapper.entityToDto(newConfig));
        }
        return config.get();
    }

    public Optional<UserConfigurationDto> getForChannel(String channelId) {
        UserConfigurationEntity config = userConfigurationRepository.findByChannelId(channelId);
        return config != null ? Optional.of(mapper.entityToDto(config)) : Optional.empty();
    }

    public UserConfigurationDto getDefaultConfiguration() {
        return mapper.entityToDto(createDefaultWithChannelId(null));
    }

    private UserConfigurationEntity createDefaultWithChannelId(String channelId) {
        UserConfigurationEntity entity = new UserConfigurationEntity();
        entity.setChannelId(channelId);
        entity.setFollowerSeconds(timerProperties.follow());
        entity.setRaiderSeconds(timerProperties.raid());
        entity.setTier1Seconds(timerProperties.tier1());
        entity.setTier2Seconds(timerProperties.tier2());
        entity.setTier3Seconds(timerProperties.tier3());
        entity.setTier1GiftSeconds(timerProperties.tier1Gift());
        entity.setTier2GiftSeconds(timerProperties.tier2Gift());
        entity.setTier3GiftSeconds(timerProperties.tier3Gift());
        entity.setCurrencySeconds(timerProperties.currency());
        entity.setBitsSeconds(timerProperties.bits());
        entity.setInitialSeconds(timerProperties.initial());
        entity.setMonetizedSecondsPerPoint(timerProperties.pointsPerSecond());
        return entity;
    }

}
