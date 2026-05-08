package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.EquipmentRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Equipment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.EquipmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.locationId()));

        Equipment equipment = new Equipment();
        equipment.setName(request.name());
        equipment.setEquipmentType(request.equipmentType());
        equipment.setLocation(location);

        return mapToResponse(equipmentRepository.save(equipment));
    }

    @Override
    public EquipmentResponse getById(Long id) {
        return mapToResponse(equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id)));
    }

    @Override
    public List<EquipmentResponse> getAll() {
        return equipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<EquipmentSummaryResponse> getByLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        return equipmentRepository.findByLocationId(locationId).stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public List<EquipmentSummaryResponse> getByType(EquipmentType type) {
        return equipmentRepository.findByEquipmentType(type).stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public List<EquipmentSummaryResponse> getByLocationAndType(Long locationId, EquipmentType type) {
        return equipmentRepository.findByLocationIdAndEquipmentType(locationId, type).stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.locationId()));

        equipment.setName(request.name());
        equipment.setEquipmentType(request.equipmentType());
        equipment.setLocation(location);

        return mapToResponse(equipmentRepository.save(equipment));
    }

    @Override
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment", id);
        }
        equipmentRepository.deleteById(id);
    }

    private EquipmentResponse mapToResponse(Equipment e) {
        return new EquipmentResponse(
                e.getId(),
                e.getName(),
                e.getEquipmentType(),
                e.getLocation().getId(),
                e.getLocation().getAddress(),
                e.getLocation().getRegion().getId(),
                e.getLocation().getRegion().getName()
        );
    }

    private EquipmentSummaryResponse mapToSummary(Equipment e) {
        return new EquipmentSummaryResponse(
                e.getId(),
                e.getName(),
                e.getEquipmentType(),
                e.getLocation().getId()
        );
    }
}
