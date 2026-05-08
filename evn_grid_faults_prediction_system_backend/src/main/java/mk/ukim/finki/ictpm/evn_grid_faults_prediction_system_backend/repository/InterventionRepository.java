package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    List<Intervention> findByFaultReportId(Long faultId);

    // Returns [crewId (Long), crewName (String), interventionCount (Long)]
    @Query("SELECT i.crew.id, i.crew.name, COUNT(i) FROM Intervention i GROUP BY i.crew.id, i.crew.name ORDER BY COUNT(i) DESC")
    List<Object[]> countByCrew();
}
