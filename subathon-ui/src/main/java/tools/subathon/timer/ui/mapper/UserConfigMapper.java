package tools.subathon.timer.ui.mapper;

import com.google.protobuf.StringValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.proto.configuration.v1.UserConfiguration;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserConfigMapper {

    @ProtoMapping
    @Mapping(target = "channelIdBytes", ignore = true)
    @Mapping(target = "mergeDonationTemplatePattern", ignore = true)
    @Mapping(target = "mergeDonationTemplateUser", ignore = true)
    UserConfiguration dtoToProto(UserConfigurationDto dto);

    UserConfigurationDto protoToDto(UserConfiguration proto);

    default StringValue stringToStringValue(String str) {
        return str != null ? StringValue.newBuilder().setValue(str).build() : StringValue.getDefaultInstance();
    }

    default String stringValueToString(StringValue stringValue) {
        return stringValue.getValue();
    }

}
