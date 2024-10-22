package by.t1d.type1diabetes.controller;

import by.t1d.type1diabetes.database.DataBase;
import by.t1d.type1diabetes.model.FoodIngestion;
import by.t1d.type1diabetes.service.GlucoseSubsystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/glucose/subsystem")
@RequiredArgsConstructor
@Controller
public class GlucoseSubsystemController {

    private final GlucoseSubsystemService glucoseSubsystemService;

    private final DataBase dataBase;

    @GetMapping
    public String showChart() {
        // Отображение пустого графика при начальной загрузке страницы
        return "chart";
    }

    @PostMapping
    public String simulateGlucoseChart(@RequestParam("carbs") double carbs,
                                       @RequestParam("duration") double duration,
                                       Model model) {
        dataBase.setFoodIngestion(new FoodIngestion(carbs, duration));

        final List<List<Double>> values = glucoseSubsystemService.forecastGlucoseDistribution();

        model.addAttribute("tDistributionValues", values.get(0));
        model.addAttribute("d1DistributionValues", values.get(1));
        model.addAttribute("d2DistributionValues", values.get(2));

        final List<List<Double>> absorptionValues = glucoseSubsystemService.forecastGlucoseAbsorption();

        model.addAttribute("tAbsorptionValues", absorptionValues.get(0));
        model.addAttribute("uGAbsorptionValues", absorptionValues.get(1));

        return "chart";
    }

}
