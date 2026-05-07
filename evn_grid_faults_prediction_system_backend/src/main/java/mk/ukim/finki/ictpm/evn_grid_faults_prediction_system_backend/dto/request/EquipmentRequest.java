package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;

public record EquipmentRequest(
        String name,
        EquipmentType type,
        Long locationId
) {}