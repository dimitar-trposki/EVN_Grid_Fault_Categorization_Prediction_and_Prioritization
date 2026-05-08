package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.RiskPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskPredictionRepository extends JpaRepository<RiskPrediction, Long> {

    // Eagerly fetches faultReport → location to avoid N+1 on map endpoint
    @Query("SELECT r FROM RiskPrediction r JOIN FETCH r.faultReport fr JOIN FETCH fr.location WHERE r.probability IS NOT NULL ORDER BY r.probability DESC")
    List<RiskPrediction> findTopRiskZonesWithLocation();
}
