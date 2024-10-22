package by.t1d.type1diabetes.database;

import by.t1d.type1diabetes.model.FoodIngestion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class DataBase {

    private FoodIngestion foodIngestion;

}
