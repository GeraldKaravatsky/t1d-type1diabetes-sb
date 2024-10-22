package by.t1d.type1diabetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FoodIngestion {

    private Double carbohydratesQuantity; //в граммах

    private Double ingestionTime; //в минутах

}
