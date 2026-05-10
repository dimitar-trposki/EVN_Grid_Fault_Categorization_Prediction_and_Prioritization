package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    Optional<Intervention> findByFaultReportId(Long faultId);

    List<Intervention> findAllByFaultReportId(Long faultId);

    List<Intervention> findByCrewId(Long crewId);

    List<Intervention> findByResolutionStatus(String resolutionStatus);

    List<Intervention> findByStartedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByCrewIdAndResolutionStatus(Long crewId, String resolutionStatus);

    // Returns [crewId (Long), crewName (String), interventionCount (Long)]
    @Query("SELECT i.crew.id, i.crew.name, COUNT(i) FROM Intervention i GROUP BY i.crew.id, i.crew.name ORDER BY COUNT(i) DESC")
    List<Object[]> countByCrew();
}
