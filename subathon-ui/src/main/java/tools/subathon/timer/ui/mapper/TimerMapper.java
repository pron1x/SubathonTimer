package tools.subathon.timer.ui.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.proto.timer.v1.Timer;
import tools.subathon.timer.proto.timer.v1.TimerState;

import java.time.Instant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimerMapper {

    TimerDto protoToDto(Timer proto);

    default Instant timestampToInstant(Timestamp value) {
        return Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
    }

    @EnumMapping(nameTransformationStrategy = MappingConstants.STRIP_PREFIX_TRANSFORMATION, configuration = "TIMER_STATE_")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.THROW_EXCEPTION)
    tools.subathon.timer.datamodel.enums.TimerState protoToEnum(TimerState value);
}
