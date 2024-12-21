package by.t1d.type1diabetes.controller;

import by.t1d.type1diabetes.dto.GlucoseInsulinSubsystemDto;
import by.t1d.type1diabetes.service.GlucoseInsulinSubsystemService;
import by.t1d.type1diabetes.util.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/glucose/subsystem")
@RequiredArgsConstructor
@Controller
public class GlucoseSubsystemController {

    private final GlucoseInsulinSubsystemService glucoseInsulinSubsystemService;

    @GetMapping
    public String showChart(Model model) {
        // Отображение пустого графика при начальной загрузке страницы
        return showEmptyCharts(model);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<GlucoseInsulinSubsystemDto> forecast(@ModelAttribute GlucoseInsulinSubsystemDto formData,
                                                               Model model) {
        if (formData.getCarbs() < 0 || formData.getCarbs() > 551) {
            model.addAttribute("errorMessage", ErrorMessage.INCORRECT_CARBS.getMessage());
            return ResponseEntity.noContent().build();
        }

        if (formData.getDuration() < 0 || formData.getDuration() > 60) {
            model.addAttribute("errorMessage", ErrorMessage.INCORRECT_DURATION.getMessage());
            return ResponseEntity.noContent().build();
        }

        final GlucoseInsulinSubsystemDto subsystemDto = glucoseInsulinSubsystemService.forecast(formData);

        return ResponseEntity.ok(subsystemDto);
    }

    private String showEmptyCharts(Model model) {
        if (!model.containsAttribute("formData")) {
            model.addAttribute("formData", new GlucoseInsulinSubsystemDto());
        }
        return "glucoseSubsystemChart";
    }

}
