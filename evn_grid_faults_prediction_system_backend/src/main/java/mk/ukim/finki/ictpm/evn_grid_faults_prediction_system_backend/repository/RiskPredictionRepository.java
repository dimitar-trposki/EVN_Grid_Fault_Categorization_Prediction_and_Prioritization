package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.RiskPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskPredictionRepository extends JpaRepository<RiskPrediction, Long> {

    List<RiskPrediction> findByLocationId(Long locationId);

    List<RiskPrediction> findByEquipmentId(Long equipmentId);

    List<RiskPrediction> findByRiskLevel(String riskLevel);

    List<RiskPrediction> findTop10ByOrderByRiskScoreDesc();

    List<RiskPrediction> findByPredictionDateAfter(LocalDateTime after);

    Optional<RiskPrediction> findTopByLocationIdOrderByPredictionDateDesc(Long locationId);

    Optional<RiskPrediction> findTopByEquipmentIdOrderByPredictionDateDesc(Long equipmentId);

    // Latest prediction per location, ordered by risk score descending — used by dashboard and getHighRiskZones
    @Query("""
            SELECT r FROM RiskPrediction r
            JOIN FETCH r.location l
            WHERE r.location IS NOT NULL
            AND r.riskScore IS NOT NULL
            AND r.predictionDate = (
                SELECT MAX(r2.predictionDate) FROM RiskPrediction r2 WHERE r2.location = r.location
            )
            ORDER BY r.riskScore DESC
            """)
    List<RiskPrediction> findTopRiskZonesWithLocation();
}
