package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByRegionId(Long regionId);

    Optional<Location> findByAddress(String address);

    List<Location> findByLongitude(Double longitude);

    List<Location> findByLatitude(Double latitude);

    Optional<Location> findByLongitudeAndLatitude(Double longitude, Double latitude);

}
