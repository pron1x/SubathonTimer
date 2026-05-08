package tools.subathon.timer.dataservice.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserConfigMapper {

    @Mapping(target = "insertTime", ignore = true)
    UserConfigurationEntity dtoToEntity(UserConfigurationDto dto);

    UserConfigurationDto entityToDto(UserConfigurationEntity entity);
}
