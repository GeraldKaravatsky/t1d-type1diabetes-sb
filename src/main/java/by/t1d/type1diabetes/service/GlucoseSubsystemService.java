package by.t1d.type1diabetes.service;

import by.t1d.type1diabetes.dto.GlucoseSubsystemDto;

import java.util.List;

public interface GlucoseSubsystemService {

    GlucoseSubsystemDto forecastGlucoseDistributionAndAbsorption(GlucoseSubsystemDto subsystemDto);

}
