package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findByName(String name);

    Optional<Crew> findByCrewCode(String crewCode);

    boolean existsByCrewCode(String crewCode);

    List<Crew> findByStatus(CrewStatus status);

    @Query("SELECT c FROM Crew c WHERE NOT EXISTS (" +
           "SELECT fa FROM FaultAssignment fa WHERE fa.crew = c " +
           "AND fa.faultStatus IN (mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus.ASSIGNED, " +
           "mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus.IN_PROGRESS))")
    List<Crew> findCrewsWithNoActiveAssignments();

}
