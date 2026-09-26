package tools.subathon.timer.ui.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "mergeFrom" ,ignore = true)
@Mapping(target = "clearField" ,ignore = true)
@Mapping(target = "clearOneof" ,ignore = true)
@Mapping(target = "unknownFields" ,ignore = true)
@Mapping(target = "mergeUnknownFields" ,ignore = true)
@Mapping(target = "allFields" ,ignore = true)
public @interface ProtoMapping {
}
