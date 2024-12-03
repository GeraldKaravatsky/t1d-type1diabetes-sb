package by.t1d.type1diabetes.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class GlucoseSubsystemDto extends ForecastResultDto {

    private LocalDateTime startTime;

    private double carbs;

    private double duration;

    private Boolean isCreateEntry;

}
