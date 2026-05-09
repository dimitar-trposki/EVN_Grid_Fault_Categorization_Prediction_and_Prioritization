package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultStatusHistoryRepository extends JpaRepository<FaultStatusHistory,Long> {
    List<FaultStatusHistory> findByFaultReportId(Long faultId);
    List<FaultStatusHistory> findByFaultReportIdOrderByChangedAtDesc(Long faultReportId);
}
