package com.pronixxx.subathon.config;

import com.pronixxx.subathon.data.entity.*;
import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig implements HasLogger {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.createTypeMap(EventEntity.class, SubathonEvent.class)
                .include(FollowEntity.class, SubathonFollowerEvent.class)
                .include(SubscribeEntity.class, SubathonSubEvent.class)
                .include(CommandEntity.class, SubathonSubEvent.class);

        modelMapper.addConverter(new Converter<EventEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<EventEntity, SubathonEvent> context) {
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
            }
        });

        modelMapper.addConverter(new Converter<FollowEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<FollowEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonFollowerEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<RaidEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<RaidEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonRaidEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<SubscribeEntity, SubathonEvent>() {

            @Override
            public SubathonEvent convert(MappingContext<SubscribeEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonSubEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<CommunityGiftEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<CommunityGiftEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonCommunityGiftEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<TipEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<TipEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonTipEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<CheerEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<CheerEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonBitCheerEvent.class));
            }
        });

        modelMapper.addConverter(new Converter<CommandEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<CommandEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonCommandEvent.class));
            }
        });

        return modelMapper;
    }
}
