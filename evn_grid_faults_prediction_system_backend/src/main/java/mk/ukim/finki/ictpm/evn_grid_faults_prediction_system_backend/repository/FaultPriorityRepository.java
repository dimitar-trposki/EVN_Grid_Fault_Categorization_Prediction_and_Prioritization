package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultPriorityRecord;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaultPriorityRepository extends JpaRepository<FaultPriorityRecord, Long> {

    Optional<FaultPriorityRecord> findByFaultReportId(Long faultReportId);

    List<FaultPriorityRecord> findByPriorityLevel(FaultPriority priorityLevel);

    List<FaultPriorityRecord> findTop10ByOrderByPriorityScoreDesc();

    List<FaultPriorityRecord> findByCalculatedAtAfter(LocalDateTime after);

    @Query(value = """
            SELECT fp.* FROM fault_priority_record fp
            JOIN fault_report fr ON fp.fault_report_id = fr.id
            JOIN (
                SELECT h.fault_report_id, h.fault_status
                FROM fault_status_history h
                JOIN (
                    SELECT fault_report_id, MAX(changed_at) AS max_at
                    FROM fault_status_history
                    GROUP BY fault_report_id
                ) m ON h.fault_report_id = m.fault_report_id AND h.changed_at = m.max_at
            ) latest ON latest.fault_report_id = fr.id
            WHERE latest.fault_status NOT IN ('RESOLVED', 'CLOSED')
            AND fp.priority_score IS NOT NULL
            ORDER BY fp.priority_score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<FaultPriorityRecord> findTopActiveFaultPriorities(@Param("limit") int limit);
}
