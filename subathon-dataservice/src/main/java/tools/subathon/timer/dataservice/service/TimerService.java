package tools.subathon.timer.dataservice.service;

import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.domain.Timer;
import tools.subathon.timer.dataservice.data.domain.TimerEvent;
import tools.subathon.timer.dataservice.data.entity.TimerEntity;
import tools.subathon.timer.dataservice.data.repository.TimerRepository;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.SubathonTipEvent;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.SubTier;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.dataservice.executor.AdjustableScheduledExecutorService;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static tools.subathon.timer.datamodel.enums.TimerState.ENDED;
import static tools.subathon.timer.datamodel.enums.TimerState.INITIALIZED;
import static tools.subathon.timer.datamodel.enums.TimerState.UNINITIALIZED;

@Service
public class TimerService implements HasLogger {

    private final BotRpcService botRpcService;
    private final SeImporterRpcService seImporterRpcService;
    private final TimerRepository timerRepository;
    private final TimerEventService timerEventService;
    private final UserConfigurationService userConfigurationService;
    private final ModelMapper mapper;

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

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


    /*
     * In-memory map of all timers by channel ID
     * Contains currently active (i.e. any timers NOT with ENDED as their state) timers, fetched on startup from the database.
     */
    private final Map<String, Timer> domainTimers = new HashMap<>();


    @Autowired
    public TimerService(TimerRepository timerRepository, TimerEventService timerEventService, UserConfigurationService userConfigurationService, ModelMapper mapper, BotRpcService botRpcService, SeImporterRpcService seImporterRpcService) {
        this.botRpcService = botRpcService;
        this.seImporterRpcService = seImporterRpcService;
        this.timerRepository = timerRepository;
        this.timerEventService = timerEventService;
        this.userConfigurationService = userConfigurationService;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        List<TimerEntity> timerList = timerRepository.findByStateIsNot(ENDED);
        getLogger().info("Found the following timers: {}", timerList);

        // This will check if a timer exists and schedule the end time for it
        for (TimerEntity timerEntity : timerList) {
            Timer domainTimer = Timer.fromDto(mapper.map(timerEntity, TimerDto.class));
            domainTimers.put(domainTimer.getChannelId(), domainTimer);

            if (domainTimer.isActive()) {
                timerControl.scheduleCommand(domainTimer.getChannelId(), () -> stopTimer(domainTimer.getChannelId()), domainTimer.getEndTime());
                timerControl.setPaused(domainTimer.getChannelId(), domainTimer.isPaused());
            }
        }
    }

    // TODO: Throw exception instead of null if errors occur
    public TimerDto initializeTimer(String channelId, String channelName) {
        // Create new timer object only if it doesn't exist yet
        if (domainTimers.containsKey(channelId)) {
            getLogger().info("Timer for channel id '{}'already exists.", channelId);
            return null;
        }
        // Make sure bot joined the channel
        if(!channelName.equals(botRpcService.requestChannelJoin(channelName))) {
            getLogger().warn("Bot is not in channel '{}' ('{}'), cannot initialize timer!", channelName, channelId);
            return null;
        }

        // Make sure SEImporter is authenticated with jwt
        UserConfigurationDto config = userConfigurationService.getForChannel(channelId);
        if (config == null) {
            getLogger().warn("No configuration for channel '{}' ('{}') found, cannot authenticate to StreamElements!", channelName, channelId);
            return null;
        }
        if (!seImporterRpcService.authenticateWithJwt(config.seJwt())) {
            getLogger().warn("Could not authenticate channel '{}' ('{}') with provided jwt!", channelName, channelId);
            return null;
        }

        Instant now = Instant.now();
        Timer domainTimer = Timer.initialize(channelId, channelName);

        // Create initial event for the timer and set timer ID
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(UNINITIALIZED);
        initialEvent.setCurrentTimerState(INITIALIZED);

        initialEvent.setTimestamp(now);

        TimerEntity timerEntity = timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));
        initialEvent.setTimerId(timerEntity.getId());
        domainTimer.setId(timerEntity.getId());
        domainTimers.put(channelId, domainTimer);

        timerEventService.save(initialEvent);
        return domainTimer.toDto();
    }

    // TODO: Throw exception instead of null on errors
    public TimerDto startTimer(String channelId, SubathonCommandEvent command) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to start it!", channelId);
            return null;
        }
        if (domainTimer.isActive()) {
            getLogger().warn("Cannot start the timer if it is already active. An active timer has to be resumed! Delegating to resumeTimer...");
            return resumeTimer(channelId, command);
        }
        getLogger().debug("Starting timer");

        // TODO: Actually use the channel config initial seconds....
        TimerEvent domainTimerEvent = domainTimer.start(Duration.ofSeconds(INITIAL_TIMER_SECONDS));
        // Schedule `stopTimer` command
        timerControl.scheduleCommand(channelId, () -> stopTimer(channelId), domainTimer.getEndTime());
        timerControl.setPaused(channelId, false);

        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);

        // Save and publish timer event
        domainTimerEvent.setSubathonEvent(command);
        timerEventService.saveAndPublish(domainTimerEvent);
        getLogger().info("Timer started. [Start: {}, End: {}]", returnTimer.startTime(), returnTimer.endTime());
        return returnTimer;
    }

    //TODO: Throw exception instead of null on errors
    public TimerDto pauseTimer(String channelId, SubathonCommandEvent command) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to pause it!", channelId);
            return null;
        }

        getLogger().debug("Pausing timer");
        // Pause scheduled execution
        TimerEvent domainTimerEvent = domainTimer.pause();
        timerControl.setPaused(channelId, true);

        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);

        // Save and publish timer event
        domainTimerEvent.setSubathonEvent(command);
        timerEventService.saveAndPublish(domainTimerEvent);
        return returnTimer;
    }

    private TimerDto resumeTimer(String channelId, SubathonCommandEvent command) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to resume it!", channelId);
            return null;
        }
        getLogger().debug("Resuming timer!");

        TimerEvent domainTimerEvent = domainTimer.resume();
        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);

        // Resume execution with new end
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Save and publish timer event
        domainTimerEvent.setSubathonEvent(command);
        timerEventService.saveAndPublish(domainTimerEvent);
        return returnTimer;
    }

    public void stopTimer(String channelId) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to stop!", channelId);
            return;
        }
        getLogger().debug("Stopping timer!");

        TimerEvent domainTimerEvent = domainTimers.get(channelId).stop();
        timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));
        domainTimers.remove(channelId);

        // Save and publish timer event
        timerEventService.saveAndPublish(domainTimerEvent);

        getLogger().info("Stopped timer for channel '{}' at {}. End timestamp: {}", domainTimer.getChannelId(), domainTimerEvent.getTimestamp(), domainTimerEvent.getCurrentEndTime());
    }

    public TimerDto addSubathonEventTime(String channelId, SubathonEvent event) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to handle subathon event '{}'!", channelId, event);
            return null;
        }
        UserConfigurationDto config = userConfigurationService.getForChannel(channelId);
        if (config == null) {
            getLogger().warn("Config for channel id '{}' not found, using fallback values!", channelId);
            config = new UserConfigurationDto(null, null, null, FOLLOWER_SECONDS, RAIDER_SECONDS, TIER_1_SECONDS, TIER_2_SECONDS, TIER_3_SECONDS,
                    TIER_1_GIFT_SECONDS, TIER_2_GIFT_SECONDS, TIER_3_GIFT_SECONDS, EURO_SECONDS, BITS_SECONDS, INITIAL_TIMER_SECONDS);
        }

        if(!domainTimer.isActive()) {
            getLogger().info("Not adding time to timer because it is not active. Ignoring {}.", event);
            return null;
        }
        getLogger().debug("Adding time for event: {}", event);

        double seconds = switch (event.getType()) {
            case FOLLOW -> config.followerSeconds();
            case RAID -> ((SubathonRaidEvent) event).getAmount() * config.raiderSeconds();
            case SUBSCRIPTION -> {
                SubathonSubEvent subEvent = (SubathonSubEvent) event;
                if (subEvent.isGifted()) {
                    yield subEvent.getTier() == SubTier.TIER_3 ? config.tier3GiftSeconds() :
                            subEvent.getTier() == SubTier.TIER_2 ? config.tier2GiftSeconds() : config.tier1GiftSeconds();
                } else {
                    yield subEvent.getTier() == SubTier.TIER_3 ? config.tier3Seconds() :
                            subEvent.getTier() == SubTier.TIER_2 ? config.tier2Seconds() : config.tier1Seconds();
                }
            }
            /* Since we cannot guarantee that community gift get send before the individual sub gifts, we add no time for them but only log!
            Time is added for the individual gifted subscriptions */
            case GIFT -> 0;
            case TIP -> config.currencySeconds() * ((SubathonTipEvent) event).getAmount();
            case CHEER -> config.bitsSeconds() * (((SubathonBitCheerEvent) event).getAmount() / 100.0);
            case COMMAND -> ((SubathonCommandEvent) event).getSeconds();
        };

        long secondsToAdd = (long) Math.ceil(seconds);
        getLogger().info("Adding {} seconds for event {}", secondsToAdd, event);
        TimerEvent domainTimerEvent = domainTimer.addTime(Duration.ofSeconds(secondsToAdd));

        // Change scheduled timer
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());
        TimerEntity returnTimer = timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));

        // Save and publish timerEvent
        domainTimerEvent.setSubathonEvent(event);
        timerEventService.saveAndPublish(domainTimerEvent);
        return mapper.map(returnTimer, TimerDto.class);
    }

    public TimerDto subtractSubathonEventTime(String channelId, SubathonCommandEvent command) {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to execute subtract command '{}'!", channelId, command);
            return null;
        }

        if(!domainTimer.isActive()) {
            getLogger().info("Not removing time from timer because it is not active. Ignoring {}.", command);
            return null;
        }
        getLogger().debug("Removing time from the timer.");

        TimerEvent domainTimerEvent = domainTimer.subtractTime(Duration.ofSeconds(command.getSeconds()));

        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());
        TimerEntity returnTimer = timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));

        // Save and publish timer event
        domainTimerEvent.setSubathonEvent(command);
        timerEventService.saveAndPublish(domainTimerEvent);
        return mapper.map(returnTimer, TimerDto.class);
    }

    public TimerDto getLatestTimerForChannel(String channelId) {
        Optional<TimerEntity> entity = timerRepository.findLatestForChannelId(channelId);
        return entity.map(timerEntity -> mapper.map(timerEntity, TimerDto.class)).orElse(null);
    }

    public List<TimerDto> getAllActiveTimers() {
        return timerRepository.findByStateIsNot(ENDED).stream().map(entity -> mapper.map(entity, TimerDto.class)).toList();
    }

}
