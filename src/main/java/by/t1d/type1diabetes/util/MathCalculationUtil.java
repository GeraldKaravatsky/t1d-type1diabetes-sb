package by.t1d.type1diabetes.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MathCalculationUtil {

    public static final int TWO_DAYS_TIME = 2880; //количество минут в двух днях (отрезок дифференцирования)

    public static final double STEP = 0.1;

    private static final int DAY_TIME = 1440;

    private MathCalculationUtil() {

    }

    public static double getStartTimeForCalculation(LocalDateTime startTime) {
        long minutesDifference = Duration.between(startTime, LocalDateTime.now()).toMinutes();

        return DAY_TIME - minutesDifference;
    }

    //Summing lists is for showing all graphs on the one axis
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

}
