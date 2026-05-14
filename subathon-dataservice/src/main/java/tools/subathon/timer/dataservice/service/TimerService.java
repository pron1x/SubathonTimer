package tools.subathon.timer.dataservice.service;

import tools.subathon.rpc.payload.response.BatchResponse;
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
import tools.subathon.timer.dataservice.service.exception.InitializationException;
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

    private final BotRpcService botRpcService;
    private final TimerRepository timerRepository;
    private final TimerEventService timerEventService;
    private final UserConfigurationService userConfigurationService;
    private final TimerMapper mapper;

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

    /*
     * In-memory map of all timers by channel ID
     * Contains currently active (i.e. any timers NOT with ENDED as their state) timers, fetched on startup from the database.
     */
    private final Map<String, Timer> domainTimers = new HashMap<>();


    @Autowired
    public TimerService(TimerRepository timerRepository, TimerEventService timerEventService, UserConfigurationService userConfigurationService, TimerMapper mapper, BotRpcService botRpcService) {
        this.botRpcService = botRpcService;
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
            Timer domainTimer = Timer.fromDto(mapper.entityToDto(timerEntity));
            try {
                initializeTwitchMessageSubscription(domainTimer.getChannelId());
                initializeAllTwitchEventSubscriptions(domainTimer.getChannelId());
            } catch (InitializationException e) {
                getLogger().warn("Could not initialize twitch event subscriptions! Skipping this timer!");
                continue;
            }
            domainTimers.put(domainTimer.getChannelId(), domainTimer);

            if (domainTimer.isActive()) {
                timerControl.scheduleCommand(domainTimer.getChannelId(), () -> {
                    try {
                        stopTimer(domainTimer.getChannelId());
                    } catch (MissingTimerException e) {
                        getLogger().error("Something went VERY wrong here! Timer with scheduled command is missing!", e);
                    }
                }, domainTimer.getEndTime());
                timerControl.setPaused(domainTimer.getChannelId(), domainTimer.isPaused());
            }
        }
    }

    private void initializeTwitchMessageSubscription(String broadcasterUserId) throws InitializationException {
        BatchResponse response;
        Optional<UserConfigurationDto> config = userConfigurationService.getForChannel(broadcasterUserId);
        String donationTemplatePattern = config.map(UserConfigurationDto::donationTemplatePattern).orElse(null);
        String donationTemplateUser = config.map(UserConfigurationDto::donationTemplateUser).orElse(null);
        for (int tries = 0; tries < 3; tries++) {
            try {
                response = botRpcService.requestMessageEventSubscription(broadcasterUserId, donationTemplatePattern, donationTemplateUser);
            } catch (RuntimeException e) {
                getLogger().error("Failed to get answer from RPC for broadcaster user id '{}', unsure if subscribed to messages! Trying again...", broadcasterUserId, e);
                continue;
            }
            if (response.batchStatus() == BatchResponse.BatchStatus.SUCCESS) {
                getLogger().info("Subscribed to messages for broadcaster user id '{}'", broadcasterUserId);
                return;
            } else {
                getLogger().info("Subscribing to message events for broadcaster user id '{}' returned status '{}', trying again...", broadcasterUserId, response.batchStatus());
            }
        }
        getLogger().warn("Failed to subscribe to message events for broadcaster user id '{}', after multiple tries! Bot might not work properly for this channel!", broadcasterUserId);
        throw new InitializationException(broadcasterUserId, "Message subscription");
    }

    private void initializeAllTwitchEventSubscriptions(String broadcasterUserId) throws InitializationException {
        BatchResponse response;
        for (int tries = 0; tries < 3; tries++) {
            try {
                response = botRpcService.requestAllEventSubscriptions(broadcasterUserId);
            } catch (RuntimeException e) {
                getLogger().error("Failed to get answer from RPC for broadcaster user id '{}', unsure if subscribed to events! Trying again...", broadcasterUserId, e);
                continue;
            }
            if (response.batchStatus() == BatchResponse.BatchStatus.SUCCESS) {
                getLogger().info("Subscribed to events for broadcaster user id '{}'", broadcasterUserId);
                return;
            } else {
                getLogger().info("Subscribing to events for broadcaster user id '{}' returned status '{}', trying again...", broadcasterUserId, response.batchStatus());
            }
        }
        getLogger().warn("Failed to subscribe to events for broadcaster user id '{}', after multiple tries! Bot might not work properly for this channel!", broadcasterUserId);
        throw new InitializationException(broadcasterUserId, "Event subscription");
    }

    public TimerDto initializeTimer(String channelId, String channelName) throws MissingChannelConfigurationException, DuplicateTimerException, InitializationException {
        if (domainTimers.containsKey(channelId)) {
            getLogger().info("Timer for channel id '{}'already exists.", channelId);
            throw new DuplicateTimerException(channelId);
        }
        // Make sure messages are subscribed to
        initializeTwitchMessageSubscription(channelId);

        // Make sure all events are subscribed to
        initializeAllTwitchEventSubscriptions(channelId);

        Optional<UserConfigurationDto> config = userConfigurationService.getForChannel(channelId);

        Timer domainTimer = Timer.initialize(channelId, channelName, config.map(UserConfigurationDto::monetizedSecondsPerPoint).orElse(null));

        timerRepository.save(mapper.dtoToEntity(domainTimer.toDto()));

        domainTimers.put(channelId, domainTimer);
        domainTimer.getAndClearPendingEvents().forEach(timerEventService::saveAndPublish);

        return domainTimer.toDto();
    }

    public TimerDto startTimer(String channelId, SubathonCommandEvent command) throws MissingTimerException, MissingChannelConfigurationException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to start it!", channelId);
            throw new MissingTimerException(channelId, "start");
        }
        if (domainTimer.isActive()) {
            getLogger().warn("Cannot start the timer if it is already active. An active timer has to be resumed! Delegating to resumeTimer...");
            return resumeTimer(channelId, command);
        }
        getLogger().debug("Starting timer");
        Optional<UserConfigurationDto> config = userConfigurationService.getForChannel(channelId);
        if(config.isEmpty()) {
            getLogger().warn("Config for channel id '{}' not found, can not start without valid config!", channelId);
            throw new MissingChannelConfigurationException(channelId);
        }

        domainTimer.start(Duration.ofSeconds(config.get().initialSeconds()));
        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(domainTimer.toDto())));
        // Schedule `stopTimer` command
        timerControl.scheduleCommand(channelId, () -> {
            try {
                stopTimer(channelId);
            } catch (MissingTimerException e) {
                getLogger().error("Something went VERY wrong here! Timer with scheduled command is missing!", e);
            }
        }, domainTimer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Save and publish timer event
        domainTimer.getAndClearPendingEvents().forEach(event -> {
            event.setSubathonEvent(command);
            timerEventService.saveAndPublish(event);
        });

        getLogger().info("Timer started. [Start: {}, End: {}]", returnTimer.startTime(), returnTimer.endTime());
        return returnTimer;
    }

    public TimerDto pauseTimer(String channelId, SubathonCommandEvent command) throws MissingTimerException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to pause it!", channelId);
            throw new MissingTimerException(channelId, "pause");
        }

        getLogger().debug("Pausing timer");
        if(domainTimer.isPaused() || !domainTimer.isActive()) {
            getLogger().info("Timer for channel id '{}' not active or already paused, not able to pause it!", channelId);
            return null;
        }
        domainTimer.pause();

        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(domainTimer.toDto())));

        timerControl.setPaused(channelId, true);

        // Save and publish timer event
        domainTimer.getAndClearPendingEvents().forEach(event -> {
            event.setSubathonEvent(command);
            timerEventService.saveAndPublish(event);
        });

        return returnTimer;
    }

    private TimerDto resumeTimer(String channelId, SubathonCommandEvent command) throws MissingTimerException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to resume it!", channelId);
            throw new MissingTimerException(channelId, "resume");
        }
        getLogger().debug("Resuming timer!");

        if(!domainTimer.isPaused() || !domainTimer.isActive()) {
            getLogger().info("Timer for channel id '{}' not active or not paused, not able to resume it!", channelId);
            return null;
        }
        domainTimer.resume();
        TimerDto returnTimer = mapper.entityToDto(timerRepository.save(mapper.dtoToEntity(domainTimer.toDto())));

        // Resume execution with new end
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Save and publish timer event
        domainTimer.getAndClearPendingEvents().forEach(event -> {
            event.setSubathonEvent(command);
            timerEventService.saveAndPublish(event);
        });

        return returnTimer;
    }

    public void stopTimer(String channelId) throws MissingTimerException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to stop!", channelId);
            throw new MissingTimerException(channelId, "stop");
        }
        getLogger().debug("Stopping timer!");

        domainTimers.get(channelId).stop();
        timerRepository.save(mapper.dtoToEntity(domainTimer.toDto()));
        domainTimers.remove(channelId);

        // Save and publish timer event
        TimerEvent domainTimerEvent = domainTimer.getAndClearPendingEvents().getFirst();

        timerEventService.saveAndPublish(domainTimerEvent);

        getLogger().info("Stopped timer for channel '{}' at {}. End timestamp: {}", domainTimer.getChannelId(), domainTimerEvent.getTimestamp(), domainTimerEvent.getCurrentEndTime());
    }

    public TimerDto addSubathonEventTime(String channelId, SubathonEvent event) throws MissingTimerException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to handle subathon event '{}'!", channelId, event);
            throw new MissingTimerException(channelId, "add time to");
        }
        UserConfigurationDto config = userConfigurationService.getForChannel(channelId).orElse(null);
        if (config == null) {
            getLogger().warn("Config for channel id '{}' not found, using fallback values!", channelId);
            config = userConfigurationService.getDefaultConfiguration();
        }

        if(!domainTimer.isActive()) {
            // TODO: Add timer not active exception?
            getLogger().info("Not adding time to timer because it is not active. Ignoring {}.", event);
            return null;
        }
        getLogger().debug("Adding time for event: {}", event);

        long secondsToAdd = getSecondsToAdd(event, config);
        getLogger().info("Adding {} seconds for event {}", secondsToAdd, event);
        domainTimer.addTime(Duration.ofSeconds(secondsToAdd));

        TimerEntity returnTimer = timerRepository.save(mapper.dtoToEntity(domainTimer.toDto()));

        // Change scheduled timer
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());

        // Save and publish timerEvent
        domainTimer.getAndClearPendingEvents().forEach(domainEvent -> {
            domainEvent.setSubathonEvent(event);
            timerEventService.saveAndPublish(domainEvent);
        });

        return mapper.entityToDto(returnTimer);
    }

    public TimerDto subtractSubathonEventTime(String channelId, SubathonCommandEvent command) throws MissingTimerException {
        Timer domainTimer = domainTimers.get(channelId);
        if (domainTimer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to execute subtract command '{}'!", channelId, command);
            throw new MissingTimerException(channelId, "subtract time from");
        }

        if(!domainTimer.isActive()) {
            // TODO: Add timer not active exception?
            getLogger().info("Not removing time from timer because it is not active. Ignoring {}.", command);
            return null;
        }
        getLogger().debug("Removing time from the timer.");

        domainTimer.subtractTime(Duration.ofSeconds(command.getSeconds()));

        TimerEntity returnTimer = timerRepository.save(mapper.dtoToEntity(domainTimer.toDto()));
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());

        // Save and publish timer event
        domainTimer.getAndClearPendingEvents().forEach(domainEvent -> {
            domainEvent.setSubathonEvent(command);
            timerEventService.saveAndPublish(domainEvent);
        });

        return mapper.entityToDto(returnTimer);
    }

    public TimerDto getLatestTimerForChannel(String channelId) {
        Optional<TimerEntity> entity = timerRepository.findLatestForChannelId(channelId);
        return entity.map(mapper::entityToDto).orElse(null);
    }

    private static long getSecondsToAdd(SubathonEvent event, UserConfigurationDto config) {
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

        return (long) Math.ceil(seconds);
    }

}
