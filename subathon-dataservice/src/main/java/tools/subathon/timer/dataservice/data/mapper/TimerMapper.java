package tools.subathon.timer.dataservice.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.dataservice.data.entity.TimerEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimerMapper {

    TimerDto entityToDto(TimerEntity entity);

    @Mapping(target = "insertTime", ignore = true)
    TimerEntity dtoToEntity(TimerDto dto);

}
