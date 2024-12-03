package by.t1d.type1diabetes.controller;

import by.t1d.type1diabetes.dto.GlucoseSubsystemDto;
import by.t1d.type1diabetes.service.GlucoseSubsystemService;
import by.t1d.type1diabetes.util.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/glucose/subsystem")
@RequiredArgsConstructor
@Controller
public class GlucoseSubsystemController {

    private final GlucoseSubsystemService glucoseSubsystemService;

    @GetMapping
    public String showChart(Model model) {
        // Отображение пустого графика при начальной загрузке страницы
        return showEmptyChart(model);
    }

    @PostMapping
    public String simulateGlucoseChart(@ModelAttribute GlucoseSubsystemDto formData,
                                       Model model) {
        if (formData.getCarbs() < 0 || formData.getCarbs() > 551) {
            model.addAttribute("errorMessage", ErrorMessage.INCORRECT_CARBS.getMessage());
            return showEmptyChart(model);
        }

        if (formData.getDuration() < 0 || formData.getDuration() > 60) {
            model.addAttribute("errorMessage", ErrorMessage.INCORRECT_DURATION.getMessage());
            return showEmptyChart(model);
        }

        final GlucoseSubsystemDto subsystemDto = glucoseSubsystemService.forecastGlucoseDistributionAndAbsorption(formData);

        model.addAttribute("tValues", subsystemDto.getTList());
        model.addAttribute("d1DistributionValues", subsystemDto.getD1List());
        model.addAttribute("d2DistributionValues", subsystemDto.getD2List());
        model.addAttribute("uGAbsorptionValues", subsystemDto.getUGList());

        model.addAttribute("formData", formData);

        return "chart";
    }

    private String showEmptyChart(Model model) {
        if (!model.containsAttribute("formData")) {
            model.addAttribute("formData", new GlucoseSubsystemDto());
        }
        return "chart";
    }

}
