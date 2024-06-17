package com.pronixxx.subathon.config;

import com.pronixxx.subathon.data.entity.EventEntity;
import com.pronixxx.subathon.data.entity.FollowEntity;
import com.pronixxx.subathon.data.entity.SubscribeEntity;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.datamodel.SubathonSubEvent;
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
                .include(SubscribeEntity.class, SubathonSubEvent.class);

        modelMapper.addConverter(new Converter<EventEntity, SubathonEvent>() {
            @Override
            public SubathonEvent convert(MappingContext<EventEntity, SubathonEvent> context) {
                EventEntity eventEntity = context.getSource();
                if(eventEntity == null) {
                    return null;
                }

                SubathonEvent eventModel = null;
                switch (eventEntity.getType()) {
                    case FOLLOW -> {
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonFollowerEvent.class));
                    }
                    case SUBSCRIPTION -> {
                        eventModel = context.getMappingEngine().map(context.create(eventEntity, SubathonSubEvent.class));
                    }
                    default -> {
                        getLogger().warn("Mapping event entity to model not yet implemented for type {} ! {}", eventEntity.getType(), eventEntity);
                    }
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

        modelMapper.addConverter(new Converter<SubscribeEntity, SubathonEvent>() {

            @Override
            public SubathonEvent convert(MappingContext<SubscribeEntity, SubathonEvent> context) {
                return context.getMappingEngine().map(context.create(context.getSource(), SubathonSubEvent.class));
            }
        });

        return modelMapper;
    }
}
