package tools.subathon.timer.dataservice.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonCommunityGiftEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.SubathonTipEvent;
import tools.subathon.timer.dataservice.data.entity.CheerEntity;
import tools.subathon.timer.dataservice.data.entity.CommandEntity;
import tools.subathon.timer.dataservice.data.entity.CommunityGiftEntity;
import tools.subathon.timer.dataservice.data.entity.EventEntity;
import tools.subathon.timer.dataservice.data.entity.FollowEntity;
import tools.subathon.timer.dataservice.data.entity.RaidEntity;
import tools.subathon.timer.dataservice.data.entity.SubscribeEntity;
import tools.subathon.timer.dataservice.data.entity.TipEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EventMapper {

    @SubclassMapping(source = CheerEntity.class, target = SubathonBitCheerEvent.class)
    @SubclassMapping(source = CommandEntity.class, target = SubathonCommandEvent.class)
    @SubclassMapping(source = CommunityGiftEntity.class, target = SubathonCommunityGiftEvent.class)
    @SubclassMapping(source = FollowEntity.class, target = SubathonFollowerEvent.class)
    @SubclassMapping(source = RaidEntity.class, target = SubathonRaidEvent.class)
    @SubclassMapping(source = SubscribeEntity.class, target = SubathonSubEvent.class)
    @SubclassMapping(source = TipEntity.class, target = SubathonTipEvent.class)
    @Mapping(target = "mock", ignore = true)
    SubathonEvent eventEntityToSubathonEvent(EventEntity eventEntity);

    @SubclassMapping(source = SubathonBitCheerEvent.class, target = CheerEntity.class)
    @SubclassMapping(source = SubathonCommandEvent.class, target = CommandEntity.class)
    @SubclassMapping(source = SubathonCommunityGiftEvent.class, target = CommunityGiftEntity.class)
    @SubclassMapping(source = SubathonFollowerEvent.class, target = FollowEntity.class)
    @SubclassMapping(source = SubathonRaidEvent.class, target = RaidEntity.class)
    @SubclassMapping(source = SubathonSubEvent.class, target = SubscribeEntity.class)
    @SubclassMapping(source = SubathonTipEvent.class, target = TipEntity.class)
    @Mapping(target = "insertTime", ignore = true)
    EventEntity subathonEventToEventEntity(SubathonEvent subathonEvent);

}
