package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;

public record EquipmentSummaryResponse(
        Long id,
        String name,
        EquipmentType equipmentType,
        Long locationId
) {}
