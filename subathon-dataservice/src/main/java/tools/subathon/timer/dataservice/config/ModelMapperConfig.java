package tools.subathon.timer.dataservice.config;

import org.modelmapper.record.RecordModule;
import tools.subathon.timer.dataservice.data.entity.CheerEntity;
import tools.subathon.timer.dataservice.data.entity.CommandEntity;
import tools.subathon.timer.dataservice.data.entity.CommunityGiftEntity;
import tools.subathon.timer.dataservice.data.entity.EventEntity;
import tools.subathon.timer.dataservice.data.entity.FollowEntity;
import tools.subathon.timer.dataservice.data.entity.RaidEntity;
import tools.subathon.timer.dataservice.data.entity.SubscribeEntity;
import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;
import tools.subathon.timer.dataservice.data.entity.TipEntity;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonCommunityGiftEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.SubathonTipEvent;
import tools.subathon.timer.datamodel.TimerEvent;

@Configuration
public class ModelMapperConfig implements HasLogger {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.registerModule(new RecordModule());

        modelMapper.createTypeMap(EventEntity.class, SubathonEvent.class)
                .include(FollowEntity.class, SubathonFollowerEvent.class)
                .include(SubscribeEntity.class, SubathonSubEvent.class)
                .include(CommandEntity.class, SubathonSubEvent.class);

        modelMapper.addConverter((Converter<EventEntity, SubathonEvent>) context -> {
            EventEntity eventEntity = context.getSource();
            if(eventEntity == null) {
                return null;
            }

            SubathonEvent eventModel = null;
            switch (eventEntity.getType()) {
                case FOLLOW ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonFollowerEvent.class));
                case RAID ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonRaidEvent.class));
                case SUBSCRIPTION ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonSubEvent.class));
                case GIFT ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonCommunityGiftEvent.class));
                case TIP ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonTipEvent.class));
                case CHEER ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonBitCheerEvent.class));
                case COMMAND ->
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonCommandEvent.class));
                default ->
                        getLogger().warn("Mapping event entity to model not yet implemented for type {} ! {}", eventEntity.getType(), eventEntity);
            }

            return eventModel;
        });

        modelMapper.addConverter((Converter<FollowEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonFollowerEvent.class)));

        modelMapper.addConverter((Converter<RaidEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonRaidEvent.class)));

        modelMapper.addConverter((Converter<SubscribeEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonSubEvent.class)));

        modelMapper.addConverter((Converter<CommunityGiftEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonCommunityGiftEvent.class)));

        modelMapper.addConverter((Converter<TipEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonTipEvent.class)));

        modelMapper.addConverter((Converter<CheerEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonBitCheerEvent.class)));

        modelMapper.addConverter((Converter<CommandEntity, SubathonEvent>) context -> context.getMappingEngine().map(context.create(context.getSource(), SubathonCommandEvent.class)));

        modelMapper.addMappings(new TimerEventToTimerEventEntityMap());

        return modelMapper;
    }

    public class SubathonEventToSubathonEventEntityConverter implements Converter<SubathonEvent, EventEntity> {

        @Override
        public EventEntity convert(MappingContext<SubathonEvent, EventEntity> mappingContext) {
            SubathonEvent source = mappingContext.getSource();
            if(source == null) {
                return null;
            }
            EventEntity output = switch (source.getType()) {
                case RAID -> {
                    SubathonRaidEvent raid = (SubathonRaidEvent) source;
                    RaidEntity raidEntity = new RaidEntity();
                    raidEntity.setAmount(raid.getAmount());
                    yield raidEntity;
                }
                case SUBSCRIPTION -> {
                    SubathonSubEvent sub = (SubathonSubEvent) source;
                    SubscribeEntity subEntity = new SubscribeEntity();
                    subEntity.setGifted(sub.isGifted());
                    subEntity.setSender(sub.getSender());
                    subEntity.setTier(sub.getTier());
                    yield subEntity;
                }
                case GIFT -> {
                    SubathonCommunityGiftEvent gift = (SubathonCommunityGiftEvent) source;
                    CommunityGiftEntity giftEntity = new CommunityGiftEntity();
                    giftEntity.setAmount(gift.getAmount());
                    giftEntity.setTier(gift.getTier());
                    yield giftEntity;
                }
                case TIP -> {
                    SubathonTipEvent tip = (SubathonTipEvent) source;
                    TipEntity tipEntity = new TipEntity();
                    tipEntity.setAmount(tip.getAmount());
                    tipEntity.setCurrency(tip.getCurrency());
                    yield tipEntity;
                }
                case CHEER -> {
                    SubathonBitCheerEvent cheer = (SubathonBitCheerEvent) source;
                    CheerEntity cheerEntity = new CheerEntity();
                    cheerEntity.setAmount(cheer.getAmount());
                    yield cheerEntity;
                }
                case COMMAND -> {
                    SubathonCommandEvent command = (SubathonCommandEvent) source;
                    CommandEntity commandEntity = new CommandEntity();
                    commandEntity.setCommand(command.getCommand());
                    commandEntity.setSeconds(command.getSeconds());
                    yield commandEntity;
                }
                case FOLLOW -> new FollowEntity();
            };
            output.setSource(source.getSource());
            output.setId(source.getId());
            output.setType(source.getType());
            output.setTimestamp(source.getTimestamp());
            output.setUsername(source.getUsername());
            return output;
        }
    }

    public class TimerEventToTimerEventEntityMap extends PropertyMap<TimerEvent, TimerEventEntity> {

        @Override
        protected void configure() {
            using(new SubathonEventToSubathonEventEntityConverter()).map(source.getSubathonEvent()).setSubathonEvent(null);
        }
    }
}
