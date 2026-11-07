package tools.subathon.timer.dataservice.service;

import org.springframework.context.ApplicationEventPublisher;
import tools.subathon.timer.datamodel.enums.EventType;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.domain.Timer;
import tools.subathon.timer.dataservice.data.domain.TimerEvent;
import tools.subathon.timer.dataservice.data.entity.TimerEntity;
import tools.subathon.timer.dataservice.data.mapper.TimerMapper;
import tools.subathon.timer.dataservice.data.repository.TimerRepository;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.SubathonTipEvent;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.SubTier;
import tools.subathon.timer.dataservice.executor.AdjustableScheduledExecutorService;
import tools.subathon.timer.dataservice.service.exception.DuplicateTimerException;
import tools.subathon.timer.dataservice.service.exception.MissingChannelConfigurationException;
import tools.subathon.timer.dataservice.service.exception.MissingTimerException;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static tools.subathon.timer.datamodel.enums.TimerState.ENDED;

@Service
public class TimerService implements HasLogger {

    private final TimerRepository timerRepository;
    private final TimerEventService timerEventService;
    private final UserConfigurationService userConfigurationService;
    private final TimerMapper mapper;

    private final ApplicationEventPublisher publisher; // TODO: Use event publisher to publish events. Check if externalizing events can happen if previous subscriber succeeded only

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

    /*
     * In-memory map of all timers by channel ID
     * Contains currently active (i.e. any timers NOT with ENDED as their state) timers, fetched on startup from the database.
     */
    private final Map<String, Timer> domainTimers = new HashMap<>();


    @Autowired
    public TimerService(TimerRepository timerRepository, TimerEventService timerEventService, UserConfigurationService userConfigurationService, TimerMapper mapper, ApplicationEventPublisher publisher) {
        this.timerRepository = timerRepository;
        this.timerEventService = timerEventService;
        this.userConfigurationService = userConfigurationService;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        List<TimerEntity> timerList = timerRepository.findByStateIsNot(ENDED);
        getLogger().info("Found the following timers: {}", timerList);

        // This will check if a timer exists and schedule the end time for it
        for (TimerEntity timerEntity : timerList) {
            Timer timer = Timer.fromDto(mapper.entityToDto(timerEntity));
            domainTimers.put(timer.getChannelId(), timer);

            if (timer.isActive()) {
                timerControl.scheduleCommand(timer.getChannelId(), () -> {
                    try {
                        stopTimer(timer.getChannelId());
                    } catch (MissingTimerException e) {
                        getLogger().error("Something went VERY wrong here! Timer with scheduled command is missing!", e);
                    }
                }, timer.getEndTime());
                timerControl.setPaused(timer.getChannelId(), timer.isPaused());
            }
        }
    }

    public TimerDto initializeTimer(String channelId, String channelName, SubathonCommandEvent commandEvent) throws DuplicateTimerException {
        if (domainTimers.containsKey(channelId)) {
            getLogger().info("Timer for channel id '{}'already exists.", channelId);
            throw new DuplicateTimerException(channelId);
        }

        Optional<UserConfigurationDto> config = userConfigurationService.getForChannel(channelId);

        Timer.TimerInitialization timerInit = Timer.initialize(channelId, channelName, config.map(UserConfigurationDto::monetizedSecondsPerPoint).orElse(null));
        Timer timer = timerInit.timer();
        TimerEvent timerEvent = timerInit.event();
        timerRepository.save(mapper.dtoToEntity(timer.toDto()));

        domainTimers.put(channelId, timer);

        timerEvent.setSubathonEvent(commandEvent);
        publisher.publishEvent(timerEvent);

        return timer.toDto();
    }

    public TimerDto startTimer(String channelId, SubathonCommandEvent commandEvent) throws MissingTimerException, MissingChannelConfigurationException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to start it!", channelId);
            throw new MissingTimerException(channelId, "start");
        }
        if (timer.isActive()) {
            getLogger().warn("Cannot start the timer if it is already active. An active timer has to be resumed! Delegating to resumeTimer...");
            return resumeTimer(channelId, commandEvent);
        }
        getLogger().debug("Starting timer");
        Optional<UserConfigurationDto> config = userConfigurationService.getForChannel(channelId);
        if(config.isEmpty()) {
            getLogger().warn("Config for channel id '{}' not found, can not start without valid config!", channelId);
            throw new MissingChannelConfigurationException(channelId);
        }

        TimerEvent timerEvent = timer.start(Duration.ofSeconds(config.get().initialSeconds()));
        timerEvent.setSubathonEvent(commandEvent);
        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(timer.toDto())));
        // Schedule `stopTimer` command
        timerControl.scheduleCommand(channelId, () -> {
            try {
                stopTimer(channelId);
            } catch (MissingTimerException e) {
                getLogger().error("Something went VERY wrong here! Timer with scheduled command is missing!", e);
            }
        }, timer.getEndTime());
        timerControl.setPaused(channelId, false);

        publisher.publishEvent(timerEvent);

        getLogger().info("Timer started. [Start: {}, End: {}]", returnTimer.startTime(), returnTimer.endTime());
        return returnTimer;
    }

    public TimerDto pauseTimer(String channelId, SubathonCommandEvent commandEvent) throws MissingTimerException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to pause it!", channelId);
            throw new MissingTimerException(channelId, "pause");
        }

        getLogger().debug("Pausing timer");
        if(timer.isPaused() || !timer.isActive()) {
            getLogger().info("Timer for channel id '{}' not active or already paused, not able to pause it!", channelId);
            return null;
        }
        TimerEvent timerEvent = timer.pause();
        timerEvent.setSubathonEvent(commandEvent);

        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(timer.toDto())));

        timerControl.setPaused(channelId, true);

        publisher.publishEvent(timerEvent);

        return returnTimer;
    }

    private TimerDto resumeTimer(String channelId, SubathonCommandEvent commandEvent) throws MissingTimerException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to resume it!", channelId);
            throw new MissingTimerException(channelId, "resume");
        }
        getLogger().debug("Resuming timer!");

        if(!timer.isPaused() || !timer.isActive()) {
            getLogger().info("Timer for channel id '{}' not active or not paused, not able to resume it!", channelId);
            return null;
        }
        TimerEvent timerEvent = timer.resume();
        timerEvent.setSubathonEvent(commandEvent);
        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(timer.toDto())));

        // Resume execution with new end
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        timerControl.setPaused(channelId, false);

        publisher.publishEvent(timerEvent);

        return returnTimer;
    }

    public void stopTimer(String channelId) throws MissingTimerException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to stop!", channelId);
            throw new MissingTimerException(channelId, "stop");
        }
        getLogger().debug("Stopping timer!");

        TimerEvent timerEvent = domainTimers.get(channelId).stop();
        timerRepository.save(mapper.dtoToEntity(timer.toDto()));
        domainTimers.remove(channelId);


        publisher.publishEvent(timerEvent);

        getLogger().info("Stopped timer for channel '{}' at {}. End timestamp: {}", timer.getChannelId(), timerEvent.getTimestamp(), timerEvent.getCurrentEndTime());
    }

    public TimerDto addSubathonEventTime(String channelId, SubathonEvent subathonEvent) throws MissingTimerException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to handle subathon event '{}'!", channelId, subathonEvent);
            throw new MissingTimerException(channelId, "add time to");
        }
        UserConfigurationDto config = userConfigurationService.getForChannel(channelId).orElse(null);
        if (config == null) {
            getLogger().warn("Config for channel id '{}' not found, using fallback values!", channelId);
            config = userConfigurationService.getDefaultConfiguration();
        }

        if(!timer.isActive()) {
            // TODO: Add timer not active exception?
            getLogger().info("Not adding time to timer because it is not active. Ignoring {}.", subathonEvent);
            return null;
        }

        if (subathonEvent.getType() == EventType.FOLLOW && timerEventService.hasUserFollowed(timer.getId(), subathonEvent.getUsername())) {
            getLogger().info("'{}' tried to follow again, ignoring follow event.", subathonEvent.getUsername());
            return null;
        }
        getLogger().debug("Adding time for event: {}", subathonEvent);

        long secondsToAdd = getSecondsToAdd(subathonEvent, config);
        getLogger().info("Adding {} seconds for event {}", secondsToAdd, subathonEvent);
        TimerEvent timerEvent = timer.addTime(Duration.ofSeconds(secondsToAdd), isMonetized(subathonEvent));
        timerEvent.setSubathonEvent(subathonEvent);
        TimerEntity returnTimer = timerRepository.save(mapper.dtoToEntity(timer.toDto()));

        // Change scheduled timer
        timerControl.setExecutionTime(channelId, timer.getEndTime());

        publisher.publishEvent(timerEvent);

        return mapper.entityToDto(returnTimer);
    }

    public TimerDto subtractSubathonEventTime(String channelId, SubathonCommandEvent commandEvent) throws MissingTimerException {
        Timer timer = domainTimers.get(channelId);
        if (timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to execute subtract command '{}'!", channelId, commandEvent);
            throw new MissingTimerException(channelId, "subtract time from");
        }

        if(!timer.isActive()) {
            // TODO: Add timer not active exception?
            getLogger().info("Not removing time from timer because it is not active. Ignoring {}.", commandEvent);
            return null;
        }
        getLogger().debug("Removing time from the timer.");

        TimerEvent timerEvent = timer.subtractTime(Duration.ofSeconds(commandEvent.getSeconds()));
        timerEvent.setSubathonEvent(commandEvent);

        TimerEntity returnTimer = timerRepository.save(mapper.dtoToEntity(timer.toDto()));
        timerControl.setExecutionTime(channelId, timer.getEndTime());

        publisher.publishEvent(timerEvent);

        return mapper.entityToDto(returnTimer);
    }

    public TimerDto getLatestTimerForChannel(String channelId) {
        Optional<TimerEntity> entity = timerRepository.findLatestForChannelId(channelId);
        return entity.map(mapper::entityToDto).orElse(null);
    }

    private static long getSecondsToAdd(SubathonEvent subathonEvent, UserConfigurationDto config) {
        double seconds = switch (subathonEvent.getType()) {
            case FOLLOW -> config.followerSeconds();
            case RAID -> ((SubathonRaidEvent) subathonEvent).getAmount() * config.raiderSeconds();
            case SUBSCRIPTION -> {
                SubathonSubEvent subEvent = (SubathonSubEvent) subathonEvent;
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
            case TIP -> config.currencySeconds() * ((SubathonTipEvent) subathonEvent).getAmount();
            case CHEER -> config.bitsSeconds() * (((SubathonBitCheerEvent) subathonEvent).getAmount() / 100.0);
            case COMMAND -> ((SubathonCommandEvent) subathonEvent).getSeconds();
        };

        return (long) Math.ceil(seconds);
    }

    private static boolean isMonetized(SubathonEvent subathonEvent) {
        return switch (subathonEvent.getType()) {
            case SUBSCRIPTION, GIFT, TIP, CHEER -> true;
            case RAID, FOLLOW, COMMAND -> false;
        };
    }

}
