package by.t1d.type1diabetes.util;

import by.t1d.type1diabetes.dto.ForecastResultDto;
import by.t1d.type1diabetes.model.CarbEntry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GlucoseMathCalculation {

    private static final int MOLECULE_GLUCOSE_WEIGHT = 180; // M_WG (Mmol)

    private static final int MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION = 40; // tauD (min)

    private static final double INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY = 0.8; // AG (unitless)

    private static final int DAY_TIME = 2880; //количество минут в одном дне (отрезок дифференцирования)

    private static final int HALF_DAY_TIME = 1440;

    private static final double STEP = 0.1;

    private GlucoseMathCalculation() {

    }

    public static ForecastResultDto forecastGlucoseDistribution(CarbEntry carbEntry) {
        List<Double> dTList = countInputGlucoseIndex(carbEntry);
        List<Double> tList = new ArrayList<>();
        List<Double> d1List = new ArrayList<>();
        List<Double> d2List = new ArrayList<>();

        double d1 = 0;
        double d2 = 0;

        double startTime = getStartTimeForCalculation(carbEntry);

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += STEP) {
            tList.add(t);
            d1List.add(d1);
            d2List.add(d2);
        }

        for (double t = 0; t <= DAY_TIME - startTime; t += STEP) {
            tList.add(t + startTime);

            double dT = t / carbEntry.getDuration() < 1 ? dTList.get((int) (t / STEP)) : 0;
            double dD1dt = INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY * dT - d1 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            double dD2dt = (d1 - d2) / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            d1 += dD1dt * STEP;
            d2 += dD2dt * STEP;

            d1List.add(d1);
            d2List.add(d2);
        }

        return ForecastResultDto.builder()
                .tList(tList)
                .d1List(d1List)
                .d2List(d2List)
                .build();
    }

    public static ForecastResultDto forecastGlucoseAbsorption(ForecastResultDto forecastResultDto) {
        List<Double> d2List = forecastResultDto.getD2List();
        List<Double> uGList = new ArrayList<>();

        for (double d2 : d2List) {
            uGList.add(d2 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION);
        }

        forecastResultDto.setUGList(uGList);

        return forecastResultDto;
    }

    public static List<Double> sumLists(List<List<Double>> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        int maxSize = source.stream().mapToInt(List::size).max().orElse(0);
        List<Double> result = new ArrayList<>();

        for (int i = 0; i < maxSize; i++) {
            double sum = 0.0;
            for (List<Double> list : source) {
                if (i < list.size()) {
                    sum += list.get(i);
                }
            }
            result.add(i, sum);
        }

        return result;
    }

    private static List<Double> countInputGlucoseIndex(CarbEntry carbEntry) {
        double v = carbEntry.getCarbs() / carbEntry.getDuration();
        double dT = 1000 * v / MOLECULE_GLUCOSE_WEIGHT;
        List<Double> dTList = new ArrayList<>();
        for (double t = 0; t <= carbEntry.getDuration(); t += STEP) {
            dTList.add(dT);
        }
        return dTList;
    }

    private static double getStartTimeForCalculation(CarbEntry carbEntry) {
        long minutesDifference = Duration.between(carbEntry.getStartTime(), LocalDateTime.now()).toMinutes();

        return HALF_DAY_TIME - minutesDifference;
    }

}
