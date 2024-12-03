package by.t1d.type1diabetes.dao;

import by.t1d.type1diabetes.model.CarbEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CarbEntryDao extends JpaRepository<CarbEntry, Long> {

    @Query("SELECT c FROM CarbEntry c WHERE c.startTime >= :timeThreshold")
    List<CarbEntry> findEntriesFromLast24Hours(@Param("timeThreshold") LocalDateTime timeThreshold);

}
