package by.t1d.type1diabetes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class ForecastGlucoseResultDto extends ForecastInsulinResultDto {

    @JsonProperty("tValues")
    private List<Double> tValues;

    private List<Double> d1DistributionValues;

    private List<Double> d2DistributionValues;

    @JsonProperty("uGAbsorptionValues")
    private List<Double> uGAbsorptionValues;

    @JsonProperty("fIValues")
    private List<Double> fIValues;

}
