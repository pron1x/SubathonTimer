package tools.subathon.timer.dataservice.data.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.dataservice.data.entity.TimerEntity;
import tools.subathon.timer.proto.timer.Timer;
import tools.subathon.timer.proto.timer.TimerState;

import java.time.Instant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimerMapper {

    TimerDto entityToDto(TimerEntity entity);

    @Mapping(target = "insertTime", ignore = true)
    TimerEntity dtoToEntity(TimerDto dto);

    @ProtoMapping
    @Mapping(target = "idBytes", ignore = true)
    @Mapping(target = "channelIdBytes" ,ignore = true)
    @Mapping(target = "channelNameBytes" ,ignore = true)
    @Mapping(target = "mergeStartTime" ,ignore = true)
    @Mapping(target = "mergeEndTime" ,ignore = true)
    @Mapping(target = "stateValue" ,ignore = true)
    @Mapping(target = "mergeUpdateTime" ,ignore = true)
    Timer dtoToProto(TimerDto dto);

    TimerDto protoToDto(Timer proto);

    default Instant timestampToInstant(Timestamp value) {
        return Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
    }

    default Timestamp instantToTimestamp(Instant value) {
        return Timestamp.newBuilder().setSeconds(value.getEpochSecond()).setNanos(value.getNano()).build();
    }

    @EnumMapping(nameTransformationStrategy = MappingConstants.PREFIX_TRANSFORMATION, configuration = "TIMER_STATE_")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.THROW_EXCEPTION)
    TimerState enumToProto(tools.subathon.timer.datamodel.enums.TimerState value);

    @EnumMapping(nameTransformationStrategy = MappingConstants.STRIP_PREFIX_TRANSFORMATION, configuration = "TIMER_STATE_")
    @ValueMapping(source = MappingConstants.ANY_UNMAPPED, target = MappingConstants.THROW_EXCEPTION)
    tools.subathon.timer.datamodel.enums.TimerState protoToEnum(TimerState value);

}
