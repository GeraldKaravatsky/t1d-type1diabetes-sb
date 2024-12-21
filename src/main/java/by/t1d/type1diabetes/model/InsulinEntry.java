package by.t1d.type1diabetes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class InsulinEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer insulinEntryId;

    private LocalDateTime startTime;

    private Double duration; //в минутах

    private Double insulinDose;

    public InsulinEntry(LocalDateTime startTime, Double duration, Double insulinDose) {
        this.startTime = startTime;
        this.duration = duration;
        this.insulinDose = insulinDose;
    }

}
