package by.t1d.type1diabetes.math.calculation;

import by.t1d.type1diabetes.dto.ForecastGlucoseResultDto;
import by.t1d.type1diabetes.model.CarbEntry;
import by.t1d.type1diabetes.util.MathCalculationUtil;

import java.util.ArrayList;
import java.util.List;

public class FoodIngestionMathCalculation {

    private FoodIngestionMathCalculation() {

    }

    public static ForecastGlucoseResultDto forecast(CarbEntry carbEntry,
                                                    ForecastGlucoseResultDto resultDto) {
        List<Double> fIList = new ArrayList<>();

        double step = MathCalculationUtil.STEP;
        double startTime = MathCalculationUtil.getStartTimeForCalculation(carbEntry.getStartTime());

        // fill with zero first part of time
        for (double t = 0; t < startTime; t += step) {
            fIList.add(0.0);
        }

        for (double t = startTime; t <= MathCalculationUtil.TWO_DAYS_TIME; t += step) {
            double timeDifference = t - startTime;

            if (timeDifference <= carbEntry.getDuration() / 2 && timeDifference >= 0) {
                fIList.add(getCommonFunctionPart(timeDifference, carbEntry));
            } else if (timeDifference <= carbEntry.getDuration() && timeDifference >= carbEntry.getDuration() / 2) {
                fIList.add(4 * carbEntry.getCarbs() / carbEntry.getDuration() -
                        getCommonFunctionPart(timeDifference, carbEntry));
            } else {
                fIList.add(0.0);
            }
        }

        resultDto.setFIValues(fIList);

        return resultDto;
    }

    private static double getCommonFunctionPart(double timeDifference, CarbEntry carbEntry) {
        return 4 * carbEntry.getCarbs() * timeDifference / (carbEntry.getDuration() * carbEntry.getDuration());
    }

}
