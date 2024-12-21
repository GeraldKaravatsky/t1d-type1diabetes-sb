package by.t1d.type1diabetes.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class GlucoseInsulinSubsystemDto extends ForecastGlucoseResultDto {

    private LocalDateTime startTime;

    private double carbs;

    private double duration;

    private LocalDateTime startTimeI;

    private double insulinDose;

    private double durationI;

    private Boolean isCreateEntry;

}
