package by.t1d.type1diabetes.service;

import java.util.List;

public interface GlucoseSubsystemService {

    List<List<Double>> forecastGlucoseDistribution();

    List<List<Double>> forecastGlucoseAbsorption();

}
