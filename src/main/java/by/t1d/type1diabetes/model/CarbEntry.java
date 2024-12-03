package by.t1d.type1diabetes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class CarbEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carbEntryId;

    private LocalDateTime startTime;

    private Double duration; //в минутах

    private Double carbs; //в граммах

    public CarbEntry(LocalDateTime startTime, Double duration, Double carbs) {
        this.startTime = startTime;
        this.duration = duration;
        this.carbs = carbs;
    }

}
