package by.t1d.type1diabetes.math.calculation;

import by.t1d.type1diabetes.dto.ForecastGlucoseResultDto;
import by.t1d.type1diabetes.model.CarbEntry;
import by.t1d.type1diabetes.util.MathCalculationUtil;

import java.util.ArrayList;
import java.util.List;

public class GlucoseMathCalculation {

    private static final int MOLECULE_GLUCOSE_WEIGHT = 180; // M_WG (Mmol)

    private static final int MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION = 40; // tauD (min)

    private static final double INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY = 0.8; // AG (unitless)

    private GlucoseMathCalculation() {

    }

    public static ForecastGlucoseResultDto forecastGlucoseDistribution(CarbEntry carbEntry,
                                                                       ForecastGlucoseResultDto resultDto) {
        List<Double> dTList = countInputGlucoseIndex(carbEntry);
        List<Double> tList = new ArrayList<>();
        List<Double> d1List = new ArrayList<>();
        List<Double> d2List = new ArrayList<>();

        double d1 = 0;
        double d2 = 0;

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(carbEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            tList.add(t);
            d1List.add(d1);
            d2List.add(d2);
        }

        for (double t = 0; t <= MathCalculationUtil.TWO_DAYS_TIME - startTime; t += step) {
            tList.add(t + startTime);

            double dT = t / carbEntry.getDuration() < 1 ? dTList.get((int) (t / step)) : 0;
            double dD1dt = INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY * dT - d1 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            double dD2dt = (d1 - d2) / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            d1 += dD1dt * step;
            d2 += dD2dt * step;

            d1List.add(d1);
            d2List.add(d2);
        }

        resultDto.setTValues(tList);
        resultDto.setD1DistributionValues(d1List);
        resultDto.setD2DistributionValues(d2List);

        return resultDto;
    }

    public static ForecastGlucoseResultDto forecastGlucoseAbsorption(ForecastGlucoseResultDto resultDto) {
        List<Double> d2List = resultDto.getD2DistributionValues();
        List<Double> uGList = new ArrayList<>();

        for (double d2 : d2List) {
            uGList.add(d2 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION);
        }

        resultDto.setUGAbsorptionValues(uGList);

        return resultDto;
    }

    private static List<Double> countInputGlucoseIndex(CarbEntry carbEntry) {
        double v = carbEntry.getCarbs() / carbEntry.getDuration();
        double dT = 1000 * v / MOLECULE_GLUCOSE_WEIGHT;
        List<Double> dTList = new ArrayList<>();
        for (double t = 0; t <= carbEntry.getDuration(); t += MathCalculationUtil.STEP) {
            dTList.add(dT);
        }
        return dTList;
    }

}
