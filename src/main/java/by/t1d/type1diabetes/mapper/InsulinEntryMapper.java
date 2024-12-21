package by.t1d.type1diabetes.mapper;

import by.t1d.type1diabetes.dto.GlucoseInsulinSubsystemDto;
import by.t1d.type1diabetes.model.InsulinEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InsulinEntryMapper {

    @Mapping(target = "startTime", source = "startTimeI")
    @Mapping(target = "duration", source = "durationI")
    @Mapping(target = "insulinDose", source = "insulinDose")
    InsulinEntry toEntity(GlucoseInsulinSubsystemDto dto);

}
