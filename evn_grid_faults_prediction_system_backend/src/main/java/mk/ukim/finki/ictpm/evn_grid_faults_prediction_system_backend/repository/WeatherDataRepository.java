package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    List<WeatherData> findByLocationId(Long locationId);

    Optional<WeatherData> findTopByLocationIdOrderByRecordedAtDesc(Long locationId);

    List<WeatherData> findByLocationIdAndRecordedAtBetween(Long locationId, LocalDateTime from, LocalDateTime to);

    void deleteByRecordedAtBefore(LocalDateTime cutoff);
}
