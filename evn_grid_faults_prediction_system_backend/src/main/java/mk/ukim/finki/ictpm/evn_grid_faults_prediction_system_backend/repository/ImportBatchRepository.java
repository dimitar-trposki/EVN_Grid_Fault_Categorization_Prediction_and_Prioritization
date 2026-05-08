package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.ImportBatch;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    List<ImportBatch> findByCreatedByUserId(Long userId);

    List<ImportBatch> findByImportStatus(BatchStatus status);
}
