package by.t1d.type1diabetes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
@Data
public class ForecastInsulinResultDto {

    @JsonProperty("iIValues")
    private List<Double> iIValues;

    private List<Double> s1Values;

    private List<Double> s2Values;

    @JsonProperty("uIValues")
    private List<Double> uIValues;

    @JsonProperty("iValues")
    private List<Double> iValues;

    private List<Double> x1Values;

    private List<Double> x2Values;

    private List<Double> x3Values;

}
