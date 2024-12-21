package by.t1d.type1diabetes.math.calculation;

import by.t1d.type1diabetes.dto.ForecastInsulinResultDto;
import by.t1d.type1diabetes.model.InsulinEntry;
import by.t1d.type1diabetes.util.MathCalculationUtil;

import java.util.ArrayList;
import java.util.List;

public class InsulinMathCalculation {

    private static final double TS = 55;

    private static final double VI = 0.12;

    private static final double KE = 0.138;

    private static final double KA1 = 0.006;

    private static final double KA2 = 0.06;

    private static final double KA3 = 0.03;

    private static final double KB1 = 0.000307;

    private static final double KB2 = 0.000492;

    private static final double KB3 = 0.0016;

    private InsulinMathCalculation() {

    }

    public static ForecastInsulinResultDto forecastInjection(InsulinEntry insulinEntry,
                                                             ForecastInsulinResultDto resultDto) {
        List<Double> iIList = new ArrayList<>();

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(insulinEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            iIList.add(0.0);
        }

        for (double t = startTime; t <= MathCalculationUtil.TWO_DAYS_TIME; t += step) {
            double timeDifference = t - startTime;

            if (timeDifference <= insulinEntry.getDuration() / 2 && timeDifference >= 0) {
                iIList.add(getCommonFunctionPart(timeDifference, insulinEntry));
            } else if (timeDifference <= insulinEntry.getDuration() && timeDifference >= insulinEntry.getDuration() / 2) {
                iIList.add(4 * insulinEntry.getInsulinDose() / insulinEntry.getDuration() -
                        getCommonFunctionPart(timeDifference, insulinEntry));
            } else {
                iIList.add(0.0);
            }
        }

        resultDto.setIIValues(iIList);

        return resultDto;
    }

    public static ForecastInsulinResultDto forecastS1AndS2(InsulinEntry insulinEntry,
                                                           ForecastInsulinResultDto resultDto) {
        List<Double> nuTList = resultDto.getIIValues();
        List<Double> s1List = new ArrayList<>();
        List<Double> s2List = new ArrayList<>();

        double s1 = 0;
        double s2 = 0;

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(insulinEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            s1List.add(s1);
            s2List.add(s2);
        }

        for (double t = startTime; t <= MathCalculationUtil.TWO_DAYS_TIME; t += step) {
            double nuT = nuTList.get((int) (t / step));
            double dS1dt = nuT - s1 / TS;
            double dS2dt = (s1 - s2) / TS;
            s1 += dS1dt * step;
            s2 += dS2dt * step;

            s1List.add(s1);
            s2List.add(s2);
        }

        resultDto.setS1Values(s1List);
        resultDto.setS2Values(s2List);

        return resultDto;
    }

    public static ForecastInsulinResultDto forecastUI(ForecastInsulinResultDto resultDto) {
        List<Double> s2List = resultDto.getS2Values();
        List<Double> uIList = new ArrayList<>();

        for (double s2 : s2List) {
            uIList.add(s2 / TS);
        }

        resultDto.setUIValues(uIList);

        return resultDto;
    }

    public static ForecastInsulinResultDto forecastI(InsulinEntry insulinEntry,
                                                     ForecastInsulinResultDto resultDto) {
        List<Double> uIList = resultDto.getUIValues();
        List<Double> iList = new ArrayList<>();

        double i = 0;

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(insulinEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            iList.add(i);
        }

        for (double t = startTime; t <= MathCalculationUtil.TWO_DAYS_TIME; t += step) {
            double uI = uIList.get((int) (t / step));
            double dIdt = uI / VI - KE * i;
            i += dIdt * step;

            iList.add(i);
        }

        resultDto.setIValues(iList);

        return resultDto;
    }

    public static ForecastInsulinResultDto forecastX1AndX2AndX3(InsulinEntry insulinEntry,
                                                                ForecastInsulinResultDto resultDto) {
        List<Double> iList = resultDto.getIValues();
        List<Double> x1List = new ArrayList<>();
        List<Double> x2List = new ArrayList<>();
        List<Double> x3List = new ArrayList<>();

        double x1 = 0;
        double x2 = 0;
        double x3 = 0;

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(insulinEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            x1List.add(x1);
            x2List.add(x2);
            x3List.add(x3);
        }

        for (double t = startTime; t <= MathCalculationUtil.TWO_DAYS_TIME; t += step) {
            double i = iList.get((int) (t / step));
            double dX1dt = KB1 * i - KA1 * x1;
            double dX2dt = KB2 * i - KA2 * x2;
            double dX3dt = KB3 * i - KA3 * x3;
            x1 += dX1dt * step;
            x2 += dX2dt * step;
            x3 += dX3dt * step;

            x1List.add(x1);
            x2List.add(x2);
            x3List.add(x3);
        }

        resultDto.setX1Values(x1List);
        resultDto.setX2Values(x2List);
        resultDto.setX3Values(x3List);

        return resultDto;
    }

    private static double getCommonFunctionPart(double timeDifference, InsulinEntry insulinEntry) {
        return 4 * insulinEntry.getInsulinDose() * timeDifference /
                (insulinEntry.getDuration() * insulinEntry.getDuration());
    }

}
