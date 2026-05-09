package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultClassificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaultClassificationResultRepository extends JpaRepository<FaultClassificationResult, Long> {

    Optional<FaultClassificationResult> findByFaultReportId(Long faultReportId);

    List<FaultClassificationResult> findByPredictedFaultCategory(String predictedFaultCategory);

    List<FaultClassificationResult> findByNlpProcessed(Boolean nlpProcessed);
}
