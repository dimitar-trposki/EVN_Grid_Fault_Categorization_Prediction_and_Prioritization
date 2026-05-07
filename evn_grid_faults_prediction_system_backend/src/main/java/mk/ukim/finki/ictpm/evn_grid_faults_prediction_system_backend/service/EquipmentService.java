package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.EquipmentRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;

import java.util.List;

public interface EquipmentService {

    EquipmentResponse create(EquipmentRequest request);

    EquipmentResponse getById(Long id);

    List<EquipmentResponse> getAll();

    EquipmentResponse update(Long id, EquipmentRequest request);

    void delete(Long id);

    List<EquipmentResponse> getByLocationId(Long locationId);

    List<EquipmentResponse> getByType(EquipmentType type);

    List<EquipmentResponse> getByLocationAndType(Long locationId, EquipmentType type);

    EquipmentResponse getByName(String name);

    List<EquipmentSummaryResponse> getSummariesByLocation(Long locationId);
}