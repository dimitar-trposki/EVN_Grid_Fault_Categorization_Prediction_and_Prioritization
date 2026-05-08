package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Equipment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByLocationId(Long locationId);

    List<Equipment> findByEquipmentType(EquipmentType equipmentType);

    List<Equipment> findByLocationIdAndEquipmentType(Long locationId, EquipmentType equipmentType);
}
