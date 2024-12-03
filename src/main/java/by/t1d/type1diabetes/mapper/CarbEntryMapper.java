package by.t1d.type1diabetes.mapper;

import by.t1d.type1diabetes.dto.GlucoseSubsystemDto;
import by.t1d.type1diabetes.model.CarbEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarbEntryMapper {

    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "duration", source = "duration")
    @Mapping(target = "carbs", source = "carbs")
    CarbEntry toEntity(GlucoseSubsystemDto dto);

    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "duration", source = "duration")
    @Mapping(target = "carbs", source = "carbs")
    GlucoseSubsystemDto toDto(CarbEntry entity);

}
