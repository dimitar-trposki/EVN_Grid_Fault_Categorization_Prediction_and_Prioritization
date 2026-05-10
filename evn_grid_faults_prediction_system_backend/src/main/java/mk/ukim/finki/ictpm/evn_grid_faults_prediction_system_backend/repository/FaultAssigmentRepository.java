package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultAssignment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaultAssigmentRepository extends JpaRepository<FaultAssignment, Long> {

    List<FaultAssignment> findByFaultReportId(Long faultId);

    List<FaultAssignment> findByCrewId(Long crewId);

    List<FaultAssignment> findByCrewIdAndAssignmentStatus(Long crewId, String assignmentStatus);

    List<FaultAssignment> findByAssignmentStatus(String assignmentStatus);

    Optional<FaultAssignment> findFirstByFaultReportIdOrderByAssignedAtDesc(Long faultReportId);

    @Query("SELECT COUNT(DISTINCT fa.crew.id) FROM FaultAssignment fa WHERE fa.faultStatus IN :statuses")
    long countDistinctActiveCrews(@Param("statuses") List<FaultStatus> statuses);

    @Query("SELECT DISTINCT fa.crew.id FROM FaultAssignment fa WHERE fa.faultStatus IN :statuses")
    List<Long> findActiveCrewIds(@Param("statuses") List<FaultStatus> statuses);
}
