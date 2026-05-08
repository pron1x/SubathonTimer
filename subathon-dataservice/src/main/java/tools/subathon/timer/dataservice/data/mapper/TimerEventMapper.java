package tools.subathon.timer.dataservice.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.dataservice.data.domain.TimerEvent;
import tools.subathon.timer.dataservice.data.entity.TimerEventEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = EventMapper.class)
public interface TimerEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "insertTime", ignore = true)
    TimerEventEntity domainToEntity(TimerEvent domain);

    TimerEventDto domainToDto(TimerEvent domain);

    TimerEvent entityToDomain(TimerEventEntity entity);

}
