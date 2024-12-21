package by.t1d.type1diabetes.dao;

import by.t1d.type1diabetes.model.InsulinEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InsulinEntryDao extends JpaRepository<InsulinEntry, Long> {

    @Query("SELECT i FROM InsulinEntry i WHERE i.startTime >= :timeThreshold")
    List<InsulinEntry> findEntriesFromLast24Hours(@Param("timeThreshold") LocalDateTime timeThreshold);

}
