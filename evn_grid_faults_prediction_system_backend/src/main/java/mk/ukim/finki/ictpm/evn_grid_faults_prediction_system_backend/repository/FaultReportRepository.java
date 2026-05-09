package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaultReportRepository extends JpaRepository<FaultReport, Long> {

    // Used by RiskPredictionService to assess fault frequency at a location
    long countByLocationId(Long locationId);

    // --- dashboard aggregations ---

    @Query(value = """
            SELECT COUNT(DISTINCT fr.id)
            FROM fault_report fr
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
            """, nativeQuery = true)
    long countActiveFaults();

    @Query(value = """
            SELECT COUNT(DISTINCT fr.id)
            FROM fault_report fr
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
              AND fr.fault_priority = 'CRITICAL'
            """, nativeQuery = true)
    long countCriticalActiveFaults();

    @Query(value = """
            SELECT COUNT(DISTINCT fr.id)
            FROM fault_report fr
            JOIN (
                SELECT fault_report_id, MIN(changed_at) AS first_at
                FROM fault_status_history
                GROUP BY fault_report_id
            ) first_rep ON first_rep.fault_report_id = fr.id
            WHERE CAST(first_rep.first_at AS DATE) = CURRENT_DATE
            """, nativeQuery = true)
    long countFaultsToday();

    @Query(value = """
            SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (ip.first_ip - r.first_rep)) / 60.0), 0.0)
            FROM (
                SELECT fault_report_id, MIN(changed_at) AS first_rep
                FROM fault_status_history
                WHERE fault_status = 'REPORTED'
                GROUP BY fault_report_id
            ) r
            JOIN (
                SELECT fault_report_id, MIN(changed_at) AS first_ip
                FROM fault_status_history
                WHERE fault_status IN ('IN_PROGRESS', 'ASSIGNED')
                GROUP BY fault_report_id
            ) ip ON ip.fault_report_id = r.fault_report_id
            WHERE ip.first_ip > r.first_rep
            """, nativeQuery = true)
    Double avgResponseTimeMinutes();

    @Query(value = """
            SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (res.first_res - r.first_rep)) / 60.0), 0.0)
            FROM (
                SELECT fault_report_id, MIN(changed_at) AS first_rep
                FROM fault_status_history
                WHERE fault_status = 'REPORTED'
                GROUP BY fault_report_id
            ) r
            JOIN (
                SELECT fault_report_id, MIN(changed_at) AS first_res
                FROM fault_status_history
                WHERE fault_status IN ('RESOLVED', 'CLOSED')
                GROUP BY fault_report_id
            ) res ON res.fault_report_id = r.fault_report_id
            WHERE res.first_res > r.first_rep
            """, nativeQuery = true)
    Double avgResolutionTimeMinutes();

    // Returns [regionId, regionName, count]
    @Query("SELECT f.location.region.id, f.location.region.name, COUNT(f) FROM FaultReport f GROUP BY f.location.region.id, f.location.region.name")
    List<Object[]> countByRegion();

    // Returns [faultType (enum), count]
    @Query("SELECT f.faultType, COUNT(f) FROM FaultReport f GROUP BY f.faultType")
    List<Object[]> countByFaultType();

    // Returns [faultReportId, latitude, longitude, fault_status, fault_priority, fault_type, reportedAt(Timestamp)]
    @Query(value = """
            SELECT
                fr.id,
                l.latitude,
                l.longitude,
                h_latest.fault_status,
                fr.fault_priority,
                fr.fault_type,
                h_first.min_at
            FROM fault_report fr
            JOIN location l ON fr.location_id = l.id
            JOIN (
                SELECT h.fault_report_id, h.fault_status
                FROM fault_status_history h
                JOIN (
                    SELECT fault_report_id, MAX(changed_at) AS max_at
                    FROM fault_status_history
                    GROUP BY fault_report_id
                ) m ON h.fault_report_id = m.fault_report_id AND h.changed_at = m.max_at
                WHERE h.fault_status NOT IN ('RESOLVED', 'CLOSED')
            ) h_latest ON h_latest.fault_report_id = fr.id
            JOIN (
                SELECT fault_report_id, MIN(changed_at) AS min_at
                FROM fault_status_history
                GROUP BY fault_report_id
            ) h_first ON h_first.fault_report_id = fr.id
            """, nativeQuery = true)
    List<Object[]> findActiveFaultsForMap();

    // Returns [period (String), count] grouped by day
    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('day', first_rep.first_at), 'YYYY-MM-DD') AS period, COUNT(*) AS cnt
            FROM (
                SELECT fault_report_id, MIN(changed_at) AS first_at
                FROM fault_status_history
                GROUP BY fault_report_id
            ) first_rep
            WHERE first_rep.first_at >= :from AND first_rep.first_at <= :to
            GROUP BY DATE_TRUNC('day', first_rep.first_at)
            ORDER BY DATE_TRUNC('day', first_rep.first_at)
            """, nativeQuery = true)
    List<Object[]> countByPeriodDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('week', first_rep.first_at), 'IYYY-"W"IW') AS period, COUNT(*) AS cnt
            FROM (
                SELECT fault_report_id, MIN(changed_at) AS first_at
                FROM fault_status_history
                GROUP BY fault_report_id
            ) first_rep
            WHERE first_rep.first_at >= :from AND first_rep.first_at <= :to
            GROUP BY DATE_TRUNC('week', first_rep.first_at)
            ORDER BY DATE_TRUNC('week', first_rep.first_at)
            """, nativeQuery = true)
    List<Object[]> countByPeriodWeek(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', first_rep.first_at), 'YYYY-MM') AS period, COUNT(*) AS cnt
            FROM (
                SELECT fault_report_id, MIN(changed_at) AS first_at
                FROM fault_status_history
                GROUP BY fault_report_id
            ) first_rep
            WHERE first_rep.first_at >= :from AND first_rep.first_at <= :to
            GROUP BY DATE_TRUNC('month', first_rep.first_at)
            ORDER BY DATE_TRUNC('month', first_rep.first_at)
            """, nativeQuery = true)
    List<Object[]> countByPeriodMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
