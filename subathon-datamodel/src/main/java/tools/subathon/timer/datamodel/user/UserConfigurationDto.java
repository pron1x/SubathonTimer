package tools.subathon.timer.datamodel.user;

public record UserConfigurationDto(
        Long id,
        String channelId,
        String seJwt,
        Integer followerSeconds,
        Integer raiderSeconds,
        Integer tier1Seconds,
        Integer tier2Seconds,
        Integer tier3Seconds,
        Integer tier1GiftSeconds,
        Integer tier2GiftSeconds,
        Integer tier3GiftSeconds,
        Integer currencySeconds,
        Integer bitsSeconds,
        Integer initialSeconds
) {
    public static UserConfigurationDto withChannelId(UserConfigurationDto userConfigurationModel, String channelId) {
        return new UserConfigurationDto(
                userConfigurationModel.id(),
                channelId,
                userConfigurationModel.seJwt(),
                userConfigurationModel.followerSeconds(),
                userConfigurationModel.raiderSeconds(),
                userConfigurationModel.tier1Seconds(),
                userConfigurationModel.tier2Seconds(),
                userConfigurationModel.tier3Seconds(),
                userConfigurationModel.tier1GiftSeconds(),
                userConfigurationModel.tier2GiftSeconds(),
                userConfigurationModel.tier3GiftSeconds(),
                userConfigurationModel.currencySeconds(),
                userConfigurationModel.bitsSeconds(),
                userConfigurationModel.initialSeconds()
        );
    }
}
