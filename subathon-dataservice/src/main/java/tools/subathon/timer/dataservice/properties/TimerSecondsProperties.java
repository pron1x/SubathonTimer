package tools.subathon.timer.dataservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "timer.seconds")
public record TimerSecondsProperties(
        @DefaultValue("10") int follow,
        @DefaultValue("1") int raid,
        @DefaultValue("60") int tier1,
        @DefaultValue("120") int tier2,
        @DefaultValue("300") int tier3,
        @DefaultValue("60") int tier1Gift,
        @DefaultValue("120") int tier2Gift,
        @DefaultValue("300") int tier3Gift,
        @DefaultValue("12") int currency,
        @DefaultValue("12") int bits,
        @DefaultValue("0") int pointsPerSecond,
        @DefaultValue("3600") int initial) {
}
