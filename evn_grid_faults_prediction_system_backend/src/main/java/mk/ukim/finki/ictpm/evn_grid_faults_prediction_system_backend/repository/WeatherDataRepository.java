package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    @Query("SELECT w FROM WeatherData w WHERE w.location.id = :locationId ORDER BY w.recordedAt DESC LIMIT 1")
    Optional<WeatherData> findLatestByLocationId(@Param("locationId") Long locationId);

    List<WeatherData> findByLocationIdAndRecordedAtBetween(Long locationId, LocalDateTime start, LocalDateTime end);
}
