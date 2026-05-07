package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.EquipmentRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Equipment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.EquipmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.EquipmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;

    @Override
    public EquipmentResponse create(EquipmentRequest request) {
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        Equipment equipment = new Equipment();
        equipment.setName(request.name());
        equipment.setEquipmentType(request.type());
        equipment.setLocation(location);

        return map(equipmentRepository.save(equipment));
    }

    @Override
    public EquipmentResponse getById(Long id) {
        return map(equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found")));
    }

    @Override
    public List<EquipmentResponse> getAll() {
        return equipmentRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        equipment.setName(request.name());
        equipment.setEquipmentType(request.type());
        equipment.setLocation(location);

        return map(equipmentRepository.save(equipment));
    }

    @Override
    public void delete(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Override
    public List<EquipmentResponse> getByLocationId(Long locationId) {
        return equipmentRepository.findByLocationId(locationId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<EquipmentResponse> getByType(EquipmentType type) {
        return equipmentRepository.findByEquipmentType(type)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<EquipmentResponse> getByLocationAndType(Long locationId, EquipmentType type) {
        return equipmentRepository.findByLocationIdAndEquipmentType(locationId, type)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public EquipmentResponse getByName(String name) {
        return map(equipmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Equipment not found")));
    }

    @Override
    public List<EquipmentSummaryResponse> getSummariesByLocation(Long locationId) {
        return equipmentRepository.findByLocationId(locationId)
                .stream()
                .map(e -> new EquipmentSummaryResponse(e.getId(), e.getName()))
                .toList();
    }

    private EquipmentResponse map(Equipment e) {
        return new EquipmentResponse(
                e.getId(),
                e.getName(),
                e.getEquipmentType(),
                e.getLocation().getId()
        );
    }
}
