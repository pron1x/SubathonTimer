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
import tools.subathon.timer.dataservice.executor.AdjustableScheduledExecutorService;
import tools.subathon.timer.dataservice.service.exception.DuplicateTimerException;
import tools.subathon.timer.dataservice.service.exception.InitializationException;
import tools.subathon.timer.dataservice.service.exception.MissingChannelConfigurationException;
import tools.subathon.timer.dataservice.service.exception.MissingTimerException;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
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
    private final SeImporterRpcService seImporterRpcService;
    private final TimerRepository timerRepository;
    private final TimerEventService timerEventService;
    private final UserConfigurationService userConfigurationService;
    private final ModelMapper mapper;

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

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

    public TimerDto initializeTimer(String channelId, String channelName) throws MissingChannelConfigurationException, DuplicateTimerException, InitializationException {
        if (domainTimers.containsKey(channelId)) {
            getLogger().info("Timer for channel id '{}'already exists.", channelId);
            throw new DuplicateTimerException(channelId);
        }
        // Make sure bot joined the channel
        if(!channelName.equals(botRpcService.requestChannelJoin(channelId))) {
            getLogger().warn("Bot is not in channel '{}' ('{}'), cannot initialize timer!", channelName, channelId);
            throw new InitializationException(channelId, "Bot channel join");
        }

        // Make sure SEImporter is authenticated with jwt
        Optional<UserConfigurationDto> configOptional = userConfigurationService.getForChannel(channelId);
        if (configOptional.isEmpty()) {
            getLogger().warn("No configuration for channel '{}' ('{}') found, cannot authenticate to StreamElements!", channelName, channelId);
            throw new MissingChannelConfigurationException(channelId);
        }
        if (!seImporterRpcService.authenticateWithJwt(configOptional.get().seJwt())) {
            getLogger().warn("Could not authenticate channel '{}' ('{}') with provided jwt!", channelName, channelId);
            throw new InitializationException(channelId, "StreamElements authentication");
        }

        Timer domainTimer = Timer.initialize(channelId, channelName);

        timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));

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
        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);
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

        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);

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
        TimerDto returnTimer = mapper.map(timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class)), TimerDto.class);

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
        timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));
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

        TimerEntity returnTimer = timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));

        // Change scheduled timer
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());

        // Save and publish timerEvent
        domainTimer.getAndClearPendingEvents().forEach(domainEvent -> {
            domainEvent.setSubathonEvent(event);
            timerEventService.saveAndPublish(domainEvent);
        });

        return mapper.map(returnTimer, TimerDto.class);
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

        TimerEntity returnTimer = timerRepository.save(mapper.map(domainTimer.toDto(), TimerEntity.class));
        timerControl.setExecutionTime(channelId, domainTimer.getEndTime());

        // Save and publish timer event
        domainTimer.getAndClearPendingEvents().forEach(domainEvent -> {
            domainEvent.setSubathonEvent(command);
            timerEventService.saveAndPublish(domainEvent);
        });

        return mapper.map(returnTimer, TimerDto.class);
    }

    public TimerDto getLatestTimerForChannel(String channelId) {
        Optional<TimerEntity> entity = timerRepository.findLatestForChannelId(channelId);
        return entity.map(timerEntity -> mapper.map(timerEntity, TimerDto.class)).orElse(null);
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
