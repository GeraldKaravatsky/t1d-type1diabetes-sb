package by.t1d.type1diabetes.dto;

import lombok.*;

import java.util.List;

@EqualsAndHashCode
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResultDto {

    private List<Double> tList;

    private List<Double> d1List;

    private List<Double> d2List;

    private List<Double> uGList;

}
