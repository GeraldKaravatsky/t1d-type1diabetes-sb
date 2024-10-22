package by.t1d.type1diabetes.service;

import by.t1d.type1diabetes.database.DataBase;
import by.t1d.type1diabetes.model.FoodIngestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GlucoseSubsystemServiceImpl implements GlucoseSubsystemService {

    private static final int MOLECULE_GLUCOSE_WEIGHT = 180; // M_WG (Mmol)

    private static final int MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION = 40; // tauD (min)

    private static final double INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY = 0.8; // AG (unitless)

    private static final int DAY_TIME = 1440; //количество минут в одном дне (отрезок дифференцирования)

    private static final double STEP = 0.1;

    private final DataBase dataBase;

    @Override
    public List<List<Double>> forecastGlucoseDistribution() {
        FoodIngestion foodIngestion = dataBase.getFoodIngestion();

        List<Double> dTList = countInputGlucoseIndex(foodIngestion);
        List<Double> tList = new ArrayList<>();
        List<Double> D1List = new ArrayList<>();
        List<Double> D2List = new ArrayList<>();
        double D1 = 0;
        double D2 = 0;
        for (double t = 0; t <= DAY_TIME; t += STEP) {
            tList.add(t);
            double dT = t / foodIngestion.getIngestionTime() < 1 ? dTList.get((int) (t / STEP)) : 0;
            double dD1dt = INDEX_OF_CARBOHYDRATE_BIO_ACTIVITY * dT - D1 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            double dD2dt = (D1 - D2) / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION;
            D1 += dD1dt * STEP;
            D2 += dD2dt * STEP;
            D1List.add(D1);
            D2List.add(D2);
        }
        List<List<Double>> result = new ArrayList<>();
        result.add(tList);
        result.add(D1List);
        result.add(D2List);
        return result;
    }

    @Override
    public List<List<Double>> forecastGlucoseAbsorption() {
        List<List<Double>> glucoseDistribution = forecastGlucoseDistribution();
        List<Double> tList = glucoseDistribution.get(0);
        List<Double> d2List = glucoseDistribution.get(2);
        List<Double> uGList = new ArrayList<>();
        for (double d2 : d2List) {
            uGList.add(d2 / MIN_MAX_TIME_CARBOHYDRATE_ABSORPTION);
        }
        List<List<Double>> result = new ArrayList<>();
        result.add(tList);
        result.add(uGList);
        return result;
    }

    private List<Double> countInputGlucoseIndex(FoodIngestion foodIngestion) {
        double v = foodIngestion.getCarbohydratesQuantity() / foodIngestion.getIngestionTime();
        double dT = 1000 * v / MOLECULE_GLUCOSE_WEIGHT;
        List<Double> dTList = new ArrayList<>();
        for (double t = 0; t <= foodIngestion.getIngestionTime(); t += STEP) {
            dTList.add(dT);
        }
        return dTList;
    }
}
