package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.ExportBatch;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportBatchRepository extends JpaRepository<ExportBatch, Long> {

    List<ExportBatch> findByCreatedByUserId(Long userId);

    List<ExportBatch> findByExportStatus(BatchStatus status);
}
